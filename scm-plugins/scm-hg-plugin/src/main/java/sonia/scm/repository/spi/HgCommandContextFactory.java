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
import sonia.scm.repository.HgConfigResolver;
import sonia.scm.repository.HgRepositoryFactory;
import sonia.scm.repository.Repository;

public class HgCommandContextFactory {

  private final HgConfigResolver configResolver;
  private final HgRepositoryFactory repositoryFactory;

  @Inject
  public HgCommandContextFactory(HgConfigResolver configResolver, HgRepositoryFactory repositoryFactory) {
    this.configResolver = configResolver;
    this.repositoryFactory = repositoryFactory;
  }

  public HgCommandContext create(Repository repository) {
    return new HgCommandContext(configResolver, repositoryFactory, repository);
  }

}
