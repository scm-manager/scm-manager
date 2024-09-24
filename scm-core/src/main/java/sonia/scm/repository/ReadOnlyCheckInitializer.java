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

import jakarta.inject.Inject;
import sonia.scm.EagerSingleton;
import sonia.scm.Initable;
import sonia.scm.SCMContextProvider;
import sonia.scm.plugin.Extension;

import java.util.Set;

/**
 * Initializes read only permissions and their checks at startup.
 */
@Extension
@EagerSingleton
final class ReadOnlyCheckInitializer implements Initable {

  private final PermissionProvider permissionProvider;
  private final Set<ReadOnlyCheck> readOnlyChecks;

  @Inject
  ReadOnlyCheckInitializer(PermissionProvider permissionProvider, Set<ReadOnlyCheck> readOnlyChecks) {
    this.permissionProvider = permissionProvider;
    this.readOnlyChecks = readOnlyChecks;
  }

  @Override
  public void init(SCMContextProvider context) {
    RepositoryPermissionGuard.setReadOnlyVerbs(permissionProvider.readOnlyVerbs());
    RepositoryPermissionGuard.setReadOnlyChecks(readOnlyChecks);
    RepositoryReadOnlyChecker.setReadOnlyChecks(readOnlyChecks);
  }
}
