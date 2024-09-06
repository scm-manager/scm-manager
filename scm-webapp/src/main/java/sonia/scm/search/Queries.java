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
