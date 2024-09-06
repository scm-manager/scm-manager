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

package sonia.scm.update.store;

import jakarta.inject.Inject;
import sonia.scm.migration.RepositoryUpdateContext;
import sonia.scm.migration.RepositoryUpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.RepositoryLocationResolver;

import java.nio.file.Path;

@Extension
public class DifferentiateBetweenConfigAndConfigEntryForRepositoryStoresUpdateStep extends DifferentiateBetweenConfigAndConfigEntryUpdateStep implements RepositoryUpdateStep {

  private final RepositoryLocationResolver locationResolver;

  @Inject
  public DifferentiateBetweenConfigAndConfigEntryForRepositoryStoresUpdateStep(RepositoryLocationResolver locationResolver) {
    this.locationResolver = locationResolver;
  }

  @Override
  public void doUpdate(RepositoryUpdateContext repositoryUpdateContext) throws Exception {
    updateAllInDirectory(locationResolver.forClass(Path.class).getLocation(repositoryUpdateContext.getRepositoryId()));
  }
}
