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
import lombok.Value;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.queryparser.flexible.standard.config.PointsConfig;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopScoreDocCollector;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LuceneQueryBuilder extends QueryBuilder {

  private static final Map<Class<?>, QueryCacheEntry> CACHE = new ConcurrentHashMap<>();

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

    Query query = Queries.filter(createQuery(queryParams, queryString), queryParams);
    try (IndexReader reader = opener.openForRead(indexName)) {
      IndexSearcher searcher = new IndexSearcher(reader);

      // TODO paging and configurable result size
      TopScoreDocCollector topScoreCollector = TopScoreDocCollector.create(10, Integer.MAX_VALUE);
      Collector collector = new PermissionAwareCollector(reader, topScoreCollector);
      searcher.search(query, collector);

      return LuceneQueryResult.of(searcher, topScoreCollector.topDocs());
    } catch (IOException e) {
      throw new SearchEngineException("failed to search index", e);
    }
  }

  private Query createQuery(QueryParams queryParams, String queryString) {
    try {
      if (queryString.contains(":")) {
        return createExpertQuery(queryParams);
      }
      return createBestGuessQuery(queryParams);
    } catch (QueryNodeException | ParseException ex) {
      throw new SearchEngineException("failed to parse query", ex);
    }
  }

  private Query createExpertQuery(QueryParams queryParams) throws QueryNodeException {
    StandardQueryParser parser = new StandardQueryParser(analyzer);
    QueryCacheEntry cacheEntry = CACHE.computeIfAbsent(queryParams.getType(), this::collectFields);
    parser.setPointsConfigMap(cacheEntry.getPointsConfig());
    return parser.parse(queryParams.getQueryString(), "");
  }

  public Query createBestGuessQuery(QueryBuilder.QueryParams queryParams) throws ParseException {
    QueryCacheEntry cacheEntry = CACHE.computeIfAbsent(queryParams.getType(), this::collectFields);
    MultiFieldQueryParser parser = new MultiFieldQueryParser(
      cacheEntry.fields, analyzer, cacheEntry.boosts
    );
    return parser.parse(queryParams.getQueryString());
  }

  private QueryCacheEntry collectFields(Class<?> type) {
    List<String> fields = new ArrayList<>();
    Map<String, Float> boosts = new HashMap<>();
    Map<String, PointsConfig> pointsConfig = new HashMap<>();

    collectFields(type, fields, boosts, pointsConfig);

    return new QueryCacheEntry(fields.toArray(new String[0]), boosts, pointsConfig);
  }

  private void collectFields(Class<?> type, List<String> fields, Map<String, Float> boosts, Map<String, PointsConfig> points) {
    Class<?> parent = type.getSuperclass();
    if (parent != null) {
      collectFields(parent, fields, boosts, points);
    }

    for (Field field : type.getDeclaredFields()) {
      Indexed indexed = field.getAnnotation(Indexed.class);
      if (indexed != null) {
        String name = indexed.name();
        if (Strings.isNullOrEmpty(name)) {
          name = field.getName();
        }
        if (indexed.defaultQuery()) {
          fields.add(name);
          if (indexed.boost() != 1f) {
            boosts.put(name, indexed.boost());
          }
        }

        PointsConfig pointsConfig = IndexableFields.pointConfig(field);
        if (pointsConfig != null) {
          points.put(name, pointsConfig);
        }
      }

    }
  }

  @Value
  private static class QueryCacheEntry {
    String[] fields;
    Map<String,Float> boosts;
    Map<String, PointsConfig> pointsConfig;
  }
}
