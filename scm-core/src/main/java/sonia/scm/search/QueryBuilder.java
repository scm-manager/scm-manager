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
import sonia.scm.NotFoundException;
import sonia.scm.repository.Repository;

import java.util.Optional;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

/**
 * Build and execute queries against an index.
 *
 * @since 2.21.0
 */
@Beta
public abstract class QueryBuilder<T> {

  private String repositoryId;
  private int start = 0;
  private int limit = 10;

  /**
   * Return only results which are related to the given repository.
   * @param repository repository
   * @return {@code this}
   */
  public QueryBuilder<T> repository(Repository repository) {
    return repository(repository.getId());
  }

  /**
   * Return only results which are related to the repository with the given id.
   * @param repositoryId id of the repository
   * @return {@code this}
   */
  public QueryBuilder<T> repository(String repositoryId) {
    this.repositoryId = repositoryId;
    return this;
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
    return execute(new QueryParams(repositoryId, queryString, start, limit));
  }


  /**
   * Executes the query and returns the total count of hits.
   *
   * @param type type of objects which are searched
   * @param queryString searched query
   *
   * @return total count of hits
   * @since 2.22.0
   */
  public QueryCountResult count(String queryString) {
    return count(new QueryParams(repositoryId, queryString, start, limit));
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
    String repositoryId;
    String queryString;
    int start;
    int limit;

    public Optional<String> getRepositoryId() {
      return Optional.ofNullable(repositoryId);
    }
  }
}
