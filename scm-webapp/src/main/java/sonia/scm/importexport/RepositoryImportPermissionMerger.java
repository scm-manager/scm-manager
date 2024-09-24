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

package sonia.scm.importexport;

import sonia.scm.repository.RepositoryPermission;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Predicate;

class RepositoryImportPermissionMerger {

  public Collection<RepositoryPermission> merge(Collection<RepositoryPermission> existingPermissions,
                                                Collection<RepositoryPermission> importedPermissions) {
    HashSet<RepositoryPermission> permissions = new HashSet<>(existingPermissions);
    importedPermissions
      .stream()
      .filter(permissionDoesNotExistYet(permissions))
      .forEach(permissions::add);

    return permissions;
  }

  private Predicate<RepositoryPermission> permissionDoesNotExistYet(HashSet<RepositoryPermission> permissions) {
    return importedPermission ->
      permissions.stream()
        .noneMatch(existingPermission ->
          existingPermission.getName().equals(importedPermission.getName())
            && existingPermission.isGroupPermission() == importedPermission.isGroupPermission()
        );
  }
}
