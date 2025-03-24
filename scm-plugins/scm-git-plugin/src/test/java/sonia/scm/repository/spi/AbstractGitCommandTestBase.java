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


import org.junit.After;
import org.junit.jupiter.api.AfterEach;
import sonia.scm.api.v2.resources.GitRepositoryConfigStoreProvider;
import sonia.scm.repository.GitConfig;
import sonia.scm.repository.GitRepositoryConfig;
import sonia.scm.store.InMemoryByteConfigurationStoreFactory;


public class AbstractGitCommandTestBase extends ZippedRepositoryTestBase {

  private GitContext context;

  @After
  @AfterEach
  public void close() {
    if (context != null) {
      context.setConfig(new GitRepositoryConfig());
      context.close();
    }
  }

  protected GitContext createContext() {
    return createContext("main");
  }

  protected GitContext createContext(String defaultBranch) {
    if (context == null) {
      GitConfig config = new GitConfig();
      config.setDefaultBranch(defaultBranch);
      GitRepositoryConfigStoreProvider storeProvider = new GitRepositoryConfigStoreProvider(new InMemoryByteConfigurationStoreFactory());
      storeProvider.setDefaultBranch(repository, defaultBranch);
      context = new GitContext(repositoryDirectory, repository, storeProvider, config);
    }

    return context;
  }

  @Override
  protected String getType() {
    return "git";
  }

  @Override
  protected String getZippedRepositoryResource() {
    return "sonia/scm/repository/spi/scm-git-spi-test.zip";
  }
}
