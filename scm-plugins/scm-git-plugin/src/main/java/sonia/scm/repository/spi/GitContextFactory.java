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

import jakarta.inject.Inject;
import sonia.scm.api.v2.resources.GitRepositoryConfigStoreProvider;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.Repository;

class GitContextFactory {

  private final GitRepositoryHandler handler;
  private final GitRepositoryConfigStoreProvider storeProvider;

  @Inject
  GitContextFactory(GitRepositoryHandler handler, GitRepositoryConfigStoreProvider storeProvider) {
    this.handler = handler;
    this.storeProvider = storeProvider;
  }

  GitContext create(Repository repository) {
    return new GitContext(handler.getDirectory(repository.getId()), repository, storeProvider, handler.getConfig());
  }

}
