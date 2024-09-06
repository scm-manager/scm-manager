/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.search;

import com.google.common.base.Strings;
import jakarta.annotation.Nonnull;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class LuceneQueryBuilder<T> extends QueryBuilder<T> {

  private static final Logger LOG = LoggerFactory.getLogger(LuceneQueryBuilder.class);
  private static final List<String> OPERATORS = List.of(
    "*", "?", "OR", "AND", "NOT", "!", "-", "+", "[", "]", "(", ")", "{", "}", "\\", ":", "\"", "^"
  );

  private final IndexManager opener;
  private final LuceneSearchableType searchableType;
  private final String indexName;
  private final Analyzer analyzer;

  LuceneQueryBuilder(IndexManager opener, String indexName, LuceneSearchableType searchableType, Analyzer analyzer) {
    this.opener = opener;
    this.indexName = indexName;
    this.searchableType = searchableType;
    this.analyzer = analyzer;
  }

  @Override
  protected QueryCountResult count(QueryParams queryParams) {
    TotalHitCountCollector totalHitCountCollector = new TotalHitCountCollector();
    return search(
      queryParams, totalHitCountCollector,
      (searcher, query) -> new QueryCountResult(
        searchableType.getType(), totalHitCountCollector.getTotalHits(), evaluateQueryType(queryParams)
      )
    );
  }

  @Override
  protected QueryResult execute(QueryParams queryParams) {
    TopScoreDocCollector topScoreCollector = createTopScoreCollector(queryParams);
    return search(queryParams, topScoreCollector, (searcher, query) -> {
      QueryResultFactory resultFactory = new QueryResultFactory(analyzer, searcher, searchableType, query);
      return resultFactory.createWithQueryType(getTopDocs(queryParams, topScoreCollector), evaluateQueryType(queryParams));
    });
  }

  private QueryType evaluateQueryType(QueryParams queryParams) {
    return containsOperator(queryParams.getQueryString()) ? QueryType.EXPERT : QueryType.SIMPLE;
  }

  private <R> R search(QueryParams queryParams, Collector collector, ResultBuilder<R> resultBuilder) {
    String queryString = Strings.nullToEmpty(queryParams.getQueryString());

    Query parsedQuery = createQuery(searchableType, queryParams, queryString);
    Query query = Queries.filter(parsedQuery, queryParams);
    if (LOG.isDebugEnabled()) {
      LOG.debug("execute lucene query: {}", query);
    }
    try (IndexReader reader = opener.openForRead(searchableType, indexName)) {
      IndexSearcher searcher = new IndexSearcher(reader);

      searcher.search(query, new PermissionAwareCollector(reader, collector));

      return resultBuilder.create(searcher, parsedQuery);
    } catch (IOException e) {
      throw new SearchEngineException("failed to search index", e);
    } catch (InvalidTokenOffsetsException e) {
      throw new SearchEngineException("failed to highlight results", e);
    }
  }

  @Nonnull
  private TopScoreDocCollector createTopScoreCollector(QueryParams queryParams) {
    return TopScoreDocCollector.create(queryParams.getStart() + queryParams.getLimit(), Integer.MAX_VALUE);
  }

  private TopDocs getTopDocs(QueryParams queryParams, TopScoreDocCollector topScoreCollector) {
    return topScoreCollector.topDocs(queryParams.getStart(), queryParams.getLimit());
  }

  private Query createQuery(LuceneSearchableType searchableType, QueryParams queryParams, String queryString) {
    try {
      return createBestGuessQuery(searchableType, queryParams);
    } catch (QueryNodeException | IOException ex) {
      throw new QueryParseException(queryString, "failed to parse query", ex);
    }
  }

  private Query createBestGuessQuery(LuceneSearchableType searchableType, QueryBuilder.QueryParams queryParams) throws QueryNodeException, IOException {
    String[] defaultFieldNames = searchableType.getDefaultFieldNames();
    if (defaultFieldNames == null || defaultFieldNames.length == 0) {
      return createExpertQuery(searchableType, queryParams, "");
    }

    String queryString = queryParams.getQueryString();
    boolean hasOperator = containsOperator(queryString);

    BooleanQuery.Builder builder = new BooleanQuery.Builder();
    for (String fieldName : defaultFieldNames) {
      Query query = hasOperator ?
        createExpertQuery(searchableType, queryParams, fieldName) :
        createWildcardQuery(fieldName, queryString);

      Float boost = searchableType.getBoosts().get(fieldName);
      if (boost != null) {
        builder.add(new BoostQuery(query, boost), BooleanClause.Occur.SHOULD);
      } else {
        builder.add(query, BooleanClause.Occur.SHOULD);
      }
    }
    return builder.build();
  }

  private Query createExpertQuery(LuceneSearchableType searchableType, QueryParams queryParams, String defaultField) throws QueryNodeException {
    StandardQueryParser parser = new StandardQueryParser(analyzer);
    parser.setPointsConfigMap(searchableType.getPointsConfig());
    return parser.parse(queryParams.getQueryString(), defaultField);
  }

  private Query createWildcardQuery(String fieldName, String queryString) throws IOException {
    try (TokenStream tokenStream = analyzer.tokenStream(fieldName, queryString)) {
      CharTermAttribute attribute = tokenStream.addAttribute(CharTermAttribute.class);

      BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();

      tokenStream.reset();
      while (tokenStream.incrementToken()) {
        String token = attribute.toString();
        queryBuilder.add(new WildcardQuery(new Term(fieldName, token + "*")), BooleanClause.Occur.SHOULD);
      }

      return queryBuilder.build();
    }
  }

  private boolean containsOperator(String queryString) {
    for (String operator : OPERATORS) {
      if (queryString.contains(operator)) {
        return true;
      }
    }

    return false;
  }

  @FunctionalInterface
  private interface ResultBuilder<T> {
    T create(IndexSearcher searcher, Query query) throws IOException, InvalidTokenOffsetsException;
  }
}
