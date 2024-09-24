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
