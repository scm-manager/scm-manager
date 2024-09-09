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

import jakarta.inject.Inject;
import sonia.scm.NotFoundException;
import sonia.scm.repository.HgConfigResolver;
import sonia.scm.repository.HgRepositoryFactory;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.spi.HgHookContextProvider;

public class HookContextProviderFactory {

  private final RepositoryManager repositoryManager;
  private final HgConfigResolver configResolver;
  private final HgRepositoryFactory repositoryFactory;

  @Inject
  public HookContextProviderFactory(RepositoryManager repositoryManager, HgConfigResolver configResolver, HgRepositoryFactory repositoryFactory) {
    this.repositoryManager = repositoryManager;
    this.configResolver = configResolver;
    this.repositoryFactory = repositoryFactory;
  }

  HgHookContextProvider create(String repositoryId, String node) {
    Repository repository = repositoryManager.get(repositoryId);
    if (repository == null) {
      throw new NotFoundException(Repository.class, repositoryId);
    }
    return new HgHookContextProvider(configResolver, repositoryFactory, repository, node);
  }

}
