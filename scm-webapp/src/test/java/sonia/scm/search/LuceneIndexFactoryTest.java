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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.user.User;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class LuceneIndexFactoryTest {

  @Mock
  private IndexManager indexManager;

  @InjectMocks
  private LuceneIndexFactory indexFactory;

  @Test
  void shouldCallOpenOnCreation() {
    LuceneIndex<Repository> index = indexFactory.create(params("default", Repository.class));
    assertThat(index.getWriter().getUsageCounter()).isOne();
  }

  @Test
  void shouldCallOpenOnReturn() {
    indexFactory.create(params("default", Repository.class));
    indexFactory.create(params("default", Repository.class));
    LuceneIndex<Repository> index = indexFactory.create(params("default", Repository.class));
    assertThat(index.getWriter().getUsageCounter()).isEqualTo(3);
  }

  @Test
  @SuppressWarnings("AssertBetweenInconvertibleTypes")
  void shouldReturnDifferentIndexForDifferentTypes() {
    LuceneIndex<Repository> repository = indexFactory.create(params("default", Repository.class));
    LuceneIndex<User> user = indexFactory.create(params("default", User.class));

    assertThat(repository).isNotSameAs(user);
  }

  @Test
  void shouldReturnDifferentIndexForDifferentIndexNames() {
    LuceneIndex<Repository> def = indexFactory.create(params("default", Repository.class));
    LuceneIndex<Repository> other = indexFactory.create(params("other", Repository.class));

    assertThat(def).isNotSameAs(other);
  }

  @Test
  void shouldReturnSameIndex() {
    LuceneIndex<Repository> one = indexFactory.create(params("default", Repository.class));
    LuceneIndex<Repository> two = indexFactory.create(params("default", Repository.class));
    assertThat(one).isSameAs(two);
  }

  private IndexParams params(String indexName, Class<?> type) {
    return new IndexParams(indexName, SearchableTypes.create(type));
  }
}
