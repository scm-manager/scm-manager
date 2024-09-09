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

package sonia.scm.repository.spi;

import sonia.scm.api.v2.resources.GitRepositoryConfigStoreProvider;
import sonia.scm.repository.GitRepositoryConfig;
import sonia.scm.repository.Repository;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.InMemoryConfigurationStore;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GitRepositoryConfigStoreProviderTestUtil {

  static GitRepositoryConfigStoreProvider createGitRepositoryConfigStoreProvider() {
    GitRepositoryConfigStoreProvider gitRepositoryConfigStoreProvider = mock(GitRepositoryConfigStoreProvider.class);
    HashMap<String, ConfigurationStore<GitRepositoryConfig>> storeMap = new HashMap<>();
    when(gitRepositoryConfigStoreProvider.get(any())).thenAnswer(invocation -> storeMap.computeIfAbsent(invocation.getArgument(0, Repository.class).getId(), id -> new InMemoryConfigurationStore<>()));
    return gitRepositoryConfigStoreProvider;
  }
}
