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

package sonia.scm.repository;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.store.InMemoryConfigurationStoreFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HgRepositoryConfigStoreTest {

  @Mock
  private Subject subject;

  private HgRepositoryConfigStore store;

  @BeforeEach
  void setUp() {
    ThreadContext.bind(subject);

    store = new HgRepositoryConfigStore(new InMemoryConfigurationStoreFactory());
  }

  @AfterEach
  void tearDownSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldThrowAuthorizationException() {
    Repository repository = RepositoryTestData.createHeartOfGold("hg");
    doThrow(AuthorizationException.class)
      .when(subject)
      .checkPermission("repository:hg:" + repository.getId());

    HgRepositoryConfig config = new HgRepositoryConfig();
    assertThrows(AuthorizationException.class, () -> store.store(repository, config));
  }

  @Test
  void shouldReadAndStore() {
    Repository repository = RepositoryTestData.createHeartOfGold("hg");
    HgRepositoryConfig config = store.of(repository);
    config.setEncoding("ISO-8859-15");
    store.store(repository, config);

    assertThat(store.of(repository).getEncoding()).isEqualTo("ISO-8859-15");
  }

}
