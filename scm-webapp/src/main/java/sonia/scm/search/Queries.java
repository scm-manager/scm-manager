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

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.util.Collection;
import java.util.Map;

import static org.apache.lucene.search.BooleanClause.Occur.MUST;
import static org.apache.lucene.search.BooleanClause.Occur.SHOULD;

final class Queries {

  private Queries() {
  }

  static Query filter(Query query, QueryBuilder.QueryParams params) {
    Map<Class<?>, Collection<String>> filters = params.getFilters();
    if (!filters.isEmpty()) {
      BooleanQuery.Builder builder = builder(filters);
      builder.add(query, MUST);
      return builder.build();
    }
    return query;
  }

  static Query filterQuery(Map<Class<?>, Collection<String>> filters) {
    return builder(filters).build();
  }

  private static BooleanQuery.Builder builder(Map<Class<?>, Collection<String>> filters) {
    BooleanQuery.Builder builder = new BooleanQuery.Builder();
    for (Map.Entry<Class<?>, Collection<String>> e : filters.entrySet()) {
      BooleanQuery.Builder filterBuilder = new BooleanQuery.Builder();
      e.getValue().forEach(
        value -> {
          Term term = createTerm(e.getKey(), value);
          filterBuilder.add(new TermQuery(term), SHOULD);
        }
      );
      builder.add(new BooleanClause(filterBuilder.build(), MUST));
    }
    return builder;
  }

  private static Term createTerm(Class<?> type, String id) {
    return new Term(Names.field(type), id);
  }
}
