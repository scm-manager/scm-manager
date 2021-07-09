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

import lombok.Value;
import sonia.scm.repository.Repository;

import java.util.Optional;

/**
 * Build and execute queries against an index.
 *
 * @since 2.21.0
 */
public abstract class QueryBuilder {

  private String repository;
  private int start = 0;
  private int limit = 10;

  /**
   * Return only results which are related to the given repository.
   * @param repository repository
   * @return {@code this}
   */
  public QueryBuilder repository(Repository repository) {
    return repository(repository.getId());
  }

  /**
   * Return only results which are related to the given repository.
   * @param repository repository
   * @return {@code this}
   */
  public QueryBuilder repository(String repository) {
    this.repository = repository;
    return this;
  }

  /**
   * The result should start at the given start.
   * All matching objects before the given start are skipped.
   * @param start start of result
   * @return {@code this}
   */
  public QueryBuilder start(int start) {
    this.start = start;
    return this;
  }

  /**
   * Defines how many hits are returned.
   * @param limit limit of hits
   * @return {@code this}
   */
  public QueryBuilder limit(int limit) {
    this.limit = limit;
    return this;
  }

  /**
   * Executes the query and returns the matched hits.
   *
   * @param type type of objects which are searched
   * @param queryString searched query
   * @return result of query
   */
  public QueryResult execute(Class<?> type, String queryString){
    return execute(new QueryParams(type, repository, queryString, start, limit));
  }

  protected abstract QueryResult execute(QueryParams queryParams);

  /**
   * The searched query and all parameters, which are belong to the query.
   */
  @Value
  static class QueryParams {
    Class<?> type;
    String repository;
    String queryString;
    int start;
    int limit;

    public Optional<String> getRepository() {
      return Optional.ofNullable(repository);
    }
  }
}
