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

package sonia.scm.repository.hooks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.NotFoundException;
import sonia.scm.repository.HgRepositoryFactory;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.spi.HgHookContextProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HookContextProviderFactoryTest {

  @Mock
  private RepositoryManager repositoryManager;

  @Mock
  private HgRepositoryHandler repositoryHandler;

  @Mock
  private HgRepositoryFactory repositoryFactory;

  @InjectMocks
  private HookContextProviderFactory factory;

  @Test
  void shouldCreateHookContextProvider() {
    when(repositoryManager.get("42")).thenReturn(RepositoryTestData.create42Puzzle());
    HgHookContextProvider provider = factory.create("42", "xyz");
    assertThat(provider).isNotNull();
  }

  @Test
  void shouldThrowNotFoundExceptionWithoutRepository() {
    assertThrows(NotFoundException.class, () -> factory.create("42", "xyz"));
  }

}
