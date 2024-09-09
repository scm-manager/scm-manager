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

import com.google.inject.Inject;
import com.google.inject.Injector;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.Repository;


@Extension
public class HgRepositoryServiceResolver implements RepositoryServiceResolver {

  private final Injector injector;
  private final HgCommandContextFactory commandContextFactory;

  @Inject
  public HgRepositoryServiceResolver(Injector injector, HgCommandContextFactory commandContextFactory) {
    this.injector = injector;
    this.commandContextFactory = commandContextFactory;
  }

  @Override
  public HgRepositoryServiceProvider resolve(Repository repository) {
    if (HgRepositoryHandler.TYPE_NAME.equalsIgnoreCase(repository.getType())) {
      return new HgRepositoryServiceProvider(injector, commandContextFactory.create(repository));
    }
    return null;
  }
}
