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

/**
 * Provider for available verbs and roles for repository permissions, such as "read", "modify", "pull", "push", etc.
 * This collection of verbs can be extended by plugins and be grouped to roles, such as "READ", "WRITE", etc.
 * The permissions are configured by "repository-permissions.xml" files from the core and from plugins.
 *
 * @since 2.12.0
 */
public interface PermissionProvider {

  /**
   * The collection of all registered verbs.
   */
  Collection<String> availableVerbs();

  /**
   * The collection of verbs that are marked as "read only". These verbs are safe for archived or otherwise read only
   * repositories.
   */
  Collection<String> readOnlyVerbs();

  /**
   * The collection of roles defined and extended by core and plugins.
   */
  Collection<RepositoryRole> availableRoles();
}
