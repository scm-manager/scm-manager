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
