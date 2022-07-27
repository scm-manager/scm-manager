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

import org.junit.jupiter.api.Test;
import sonia.scm.group.Group;
import sonia.scm.repository.Repository;
import sonia.scm.user.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class QueryBuilderTest {

  private final QueryBuilder<String> queryBuilder = new CapturingQueryBuilder<>();

  private QueryBuilder.QueryParams params;

  @Test
  void shouldCreateFilterForRepository() {
    Repository repository = new Repository();
    repository.setId("hog");

    queryBuilder.filter(repository).execute("awesome");

    assertThat(params.getFilters()).containsEntry(Repository.class, List.of("hog"));
  }

  @Test
  void shouldCreateFilterMap() {
    queryBuilder.filter(User.class, "one")
      .filter(Group.class, new Group("xml", "crew"))
      .count("awesome");

    assertThat(params.getFilters())
      .containsEntry(User.class, List.of("one"))
      .containsEntry(Group.class, List.of("crew"));
  }

  @Test
  void shouldPassPagingParameters() {
    queryBuilder.start(10).limit(25).execute("...");

    assertThat(params.getStart()).isEqualTo(10);
    assertThat(params.getLimit()).isEqualTo(25);
  }

  @Test
  void shouldCreateParamsWithDefaults() {
    queryBuilder.execute("hello");

    assertThat(params.getQueryString()).isEqualTo("hello");
    assertThat(params.getFilters()).isEmpty();
    assertThat(params.getStart()).isZero();
    assertThat(params.getLimit()).isEqualTo(10);
  }

  private class CapturingQueryBuilder<T> extends QueryBuilder<T> {

    @Override
    protected QueryResult execute(QueryParams queryParams) {
      QueryBuilderTest.this.params = queryParams;
      return null;
    }
  }

}
