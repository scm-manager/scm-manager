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

import java.util.Collection;
import java.util.Optional;

/**
 * This abstracts the permissions of {@link Repository} and {@link Namespace} objects.
 */
public interface RepositoryPermissionHolder {

  /**
   * Returns a collection of all permissions for this object.
   */
  Collection<RepositoryPermission> getPermissions();

  /**
   * Sets and therefore overwrites the permissions for this object.
   *
   * @param permissions The new permissions for this object.
   */
  void setPermissions(Collection<RepositoryPermission> permissions);

  /**
   * Adds a single permission to the current set of permissions for this object.
   *
   * @param newPermission The new permission that will be added to the existing permissions.
   */
  void addPermission(RepositoryPermission newPermission);

  /**
   * Removes a single permission from the current set of permissions for this object.
   *
   * @param permission The permission that should be removed from the existing permissions.
   * @return <code>true</code>, if the given permission was part of the permissions for this object, <code>false</code>
   * otherwise.
   */
  boolean removePermission(RepositoryPermission permission);

  /**
   * Returns the permission for the given user, if present, or an empty {@link Optional} otherwise.
   *
   * @since 2.38.0
   */
  default Optional<RepositoryPermission> findUserPermission(String userId) {
    return findPermission(userId, false);
  }

  /**
   * Returns the permission for the given group, if present, or an empty {@link Optional} otherwise.
   *
   * @since 2.38.0
   */
  default Optional<RepositoryPermission> findGroupPermission(String groupId) {
    return findPermission(groupId, true);
  }

  private Optional<RepositoryPermission> findPermission(String name, boolean isGroup) {
    return getPermissions().stream().filter(p -> p.isGroupPermission() == isGroup && p.getName().equals(name)).findFirst();
  }
}
