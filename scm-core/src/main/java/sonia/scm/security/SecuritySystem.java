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

package sonia.scm.security;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * The SecuritySystem manages global permissions.
 *
 * @since 1.31
 */
public interface SecuritySystem {

  /**
   * Store a new permission.
   */
  void addPermission(AssignedPermission permission);

  /**
   * Delete stored permission.
   */
  void deletePermission(AssignedPermission permission);

  /**
   * Return all available permissions.
   */
  Collection<PermissionDescriptor> getAvailablePermissions();

  /**
   * Returns all stored permissions which are matched by the given
   * {@link Predicate}.
   */
  Collection<AssignedPermission> getPermissions(Predicate<AssignedPermission> predicate);
}
