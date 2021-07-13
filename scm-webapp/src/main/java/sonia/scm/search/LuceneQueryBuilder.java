/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.search;

import com.google.common.base.Strings;
import org.apache.lucene.analysis.Analyzer;
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
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LuceneQueryBuilder extends QueryBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(LuceneQueryBuilder.class);

  private static final Map<Class<?>, SearchableType> CACHE = new ConcurrentHashMap<>();

  private final IndexOpener opener;
  private final String indexName;
  private final Analyzer analyzer;

  LuceneQueryBuilder(IndexOpener opener, String indexName, Analyzer analyzer) {
    this.opener = opener;
    this.indexName = indexName;
    this.analyzer = analyzer;
  }

  @Override
  protected QueryResult execute(QueryParams queryParams) {
    String queryString = Strings.nullToEmpty(queryParams.getQueryString());

    SearchableType searchableType = CACHE.computeIfAbsent(queryParams.getType(), SearchableTypes::create);

    Query query = Queries.filter(createQuery(searchableType, queryParams, queryString), queryParams);
    if (LOG.isDebugEnabled()) {
      LOG.debug("execute lucene query: {}", query);
    }
    try (IndexReader reader = opener.openForRead(indexName)) {
      IndexSearcher searcher = new IndexSearcher(reader);

      TopScoreDocCollector topScoreCollector = createTopScoreCollector(queryParams);
      Collector collector = new PermissionAwareCollector(reader, topScoreCollector);
      searcher.search(query, collector);

      QueryResultFactory resultFactory = new QueryResultFactory(analyzer, searcher, searchableType, query);
      return resultFactory.create(getTopDocs(queryParams, topScoreCollector));
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

  private Query createQuery(SearchableType searchableType, QueryParams queryParams, String queryString) {
    try {
      if (queryString.contains(":")) {
        return createExpertQuery(searchableType, queryParams);
      }
      return createBestGuessQuery(searchableType, queryParams);
    } catch (QueryNodeException ex) {
      throw new QueryParseException(queryString, "failed to parse query", ex);
    }
  }

  private Query createExpertQuery(SearchableType searchableType, QueryParams queryParams) throws QueryNodeException {
    StandardQueryParser parser = new StandardQueryParser(analyzer);

    parser.setPointsConfigMap(searchableType.getPointsConfig());
    return parser.parse(queryParams.getQueryString(), "");
  }

  public Query createBestGuessQuery(SearchableType searchableType, QueryBuilder.QueryParams queryParams) {
    String[] fieldNames = searchableType.getFieldNames();
    if (fieldNames == null || fieldNames.length == 0) {
      throw new NoDefaultQueryFieldsFoundException("no default query fields defined for " + searchableType.getType());
    }
    BooleanQuery.Builder builder = new BooleanQuery.Builder();
    for (String fieldName : fieldNames) {
      Term term = new Term(fieldName, appendWildcardIfNotAlreadyUsed(queryParams));
      WildcardQuery query = new WildcardQuery(term);

      Float boost = searchableType.getBoosts().get(fieldName);
      if (boost != null) {
        builder.add(new BoostQuery(query, boost), BooleanClause.Occur.SHOULD);
      } else {
        builder.add(query, BooleanClause.Occur.SHOULD);
      }
    }
    return builder.build();
  }

  @Nonnull
  private String appendWildcardIfNotAlreadyUsed(QueryParams queryParams) {
    String queryString = queryParams.getQueryString().toLowerCase(Locale.ENGLISH);
    if (!queryString.contains("?") && !queryString.contains("*")) {
      queryString += "*";
    }
    return queryString;
  }
}
