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
