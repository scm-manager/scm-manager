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

public abstract class QueryBuilder {

  private String repository;
  private int start = 0;
  private int limit = 10;

  public QueryBuilder repository(Repository repository) {
    return repository(repository.getId());
  }

  public QueryBuilder repository(String repository) {
    this.repository = repository;
    return this;
  }

  public QueryBuilder start(int start) {
    this.start = start;
    return this;
  }

  public QueryBuilder limit(int limit) {
    this.limit = limit;
    return this;
  }

  public QueryResult execute(Class<?> type, String queryString){
    return execute(new QueryParams(type, repository, queryString, start, limit));
  }

  protected abstract QueryResult execute(QueryParams queryParams);

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
