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

import com.google.common.annotations.Beta;
import lombok.Value;
import sonia.scm.ModelObject;
import sonia.scm.repository.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;

/**
 * Build and execute queries against an index.
 *
 * @param <T> type of indexed objects
 * @since 2.21.0
 */
@Beta
public abstract class QueryBuilder<T> {

  private final Map<Class<?>, Collection<String>> filters = new HashMap<>();
  private int start = 0;
  private int limit = 10;

  /**
   * Return only results which are related to the given part of the id.
   * Note: this function can be called multiple times.
   * @param type type of id part
   * @param id value of id part
   * @return {@code this}
   * @since 2.23.0
   * @see Id#and(Class, String)
   */
  public QueryBuilder<T> filter(Class<?> type, String id) {
    addToFilter(type, id);
    return this;
  }

  /**
   * Return only results which are related to the given repository.
   * @param repository repository for filter
   * @return {@code this}
   * @since 2.23.0
   * @see Id#and(Class, String)
   */
  public QueryBuilder<T> filter(Repository repository) {
    addToFilter(Repository.class, repository.getId());
    return this;
  }

  private void addToFilter(Class<?> type, String id) {
    if (filters.containsKey(type)) {
      filters.get(type).add(id);
    } else {
      filters.put(type, new ArrayList<>(asList(id)));
    }
  }

  /**
   * Return only results which are related to the given part of the id.
   * Note: this function can be called multiple times.
   * @param type type of id part
   * @param idObject object which holds the id value
   * @return {@code this}
   * @since 2.23.0
   * @see Id#and(Class, ModelObject)
   */
  public QueryBuilder<T> filter(Class<?> type, ModelObject idObject) {
    return filter(type, idObject.getId());
  }

  /**
   * The result should start at the given number.
   * All matching objects before the given start are skipped.
   * @param start start of result
   * @return {@code this}
   */
  public QueryBuilder<T> start(int start) {
    this.start = start;
    return this;
  }

  /**
   * Defines how many hits are returned.
   * @param limit limit of hits
   * @return {@code this}
   */
  public QueryBuilder<T> limit(int limit) {
    this.limit = limit;
    return this;
  }

  /**
   * Executes the query and returns the matches.
   *
   * @param queryString searched query
   * @return result of query
   */
  public QueryResult execute(String queryString){
    return execute(new QueryParams(queryString, filters, start, limit));
  }


  /**
   * Executes the query and returns the total count of hits.
   *
   * @param queryString searched query
   *
   * @return total count of hits
   * @since 2.22.0
   */
  public QueryCountResult count(String queryString) {
    return count(new QueryParams(queryString, filters, start, limit));
  }


  /**
   * Executes the query and returns the matches.
   * @param queryParams query parameter
   * @return result of query
   */
  protected abstract QueryResult execute(QueryParams queryParams);

  /**
   * Executes the query and returns the total count of hits.
   * @param queryParams query parameter
   * @return total hit count
   * @since 2.22.0
   */
  protected QueryCountResult count(QueryParams queryParams) {
    return execute(queryParams);
  }

  /**
   * The searched query and all parameters, which belong to the query.
   */
  @Value
  static class QueryParams {
    String queryString;
    Map<Class<?>, Collection<String>> filters;
    int start;
    int limit;

  }
}
