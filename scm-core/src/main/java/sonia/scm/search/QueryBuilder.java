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

import com.google.common.annotations.Beta;
import lombok.Value;
import sonia.scm.ModelObject;
import sonia.scm.repository.Repository;

import java.util.HashMap;
import java.util.Map;

/**
 * Build and execute queries against an index.
 *
 * @param <T> type of indexed objects
 * @since 2.21.0
 */
@Beta
public abstract class QueryBuilder<T> {

  private final Map<Class<?>, String> filters = new HashMap<>();
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
    filters.put(type, id);
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
    filters.put(Repository.class, repository.getId());
    return this;
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
    Map<Class<?>, String> filters;
    int start;
    int limit;

  }
}
