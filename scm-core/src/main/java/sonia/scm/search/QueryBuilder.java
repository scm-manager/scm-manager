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
import sonia.scm.ContextEntry;
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
public abstract class QueryBuilder {

  private String repositoryId;
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
   * Return only results which are related to the repository with the given id.
   * @param repositoryId id of the repository
   * @return {@code this}
   */
  public QueryBuilder repository(String repositoryId) {
    this.repositoryId = repositoryId;
    return this;
  }

  /**
   * The result should start at the given number.
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
   * Executes the query and returns the matches.
   *
   * @param type type of objects which are searched
   * @param queryString searched query
   * @return result of query
   */
  public QueryResult execute(Class<?> type, String queryString){
    return execute(new QueryParams(type, repositoryId, queryString, start, limit));
  }

  /**
   * Executes the query and returns the matches.
   *
   * @param typeName type name of objects which are searched
   * @param queryString searched query
   * @return result of query
   *
   * @throws NotFoundException if type could not be found
   */
  public QueryResult execute(String typeName, String queryString){
    Class<?> type = resolveByName(typeName).orElseThrow(() -> notFound(entity("type", typeName)));
    return execute(type, queryString);
  }

  /**
   * Resolves the type by its name. Returns optional with class of type or an empty optional.
   *
   * @param typeName name of type
   * @return optional with class of type or empty
   */
  protected abstract Optional<Class<?>> resolveByName(String typeName);

  /**
   * Executes the query and returns the matches.
   * @param queryParams query parameter
   * @return result of query
   */
  protected abstract QueryResult execute(QueryParams queryParams);

  /**
   * The searched query and all parameters, which belong to the query.
   */
  @Value
  static class QueryParams {
    Class<?> type;
    String repositoryId;
    String queryString;
    int start;
    int limit;

    public Optional<String> getRepositoryId() {
      return Optional.ofNullable(repositoryId);
    }
  }
}
