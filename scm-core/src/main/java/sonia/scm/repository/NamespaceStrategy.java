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

import sonia.scm.plugin.ExtensionPoint;

/**
 * Strategy to create a namespace for the new repository. Namespaces are used to order and identify repositories.
 */
@ExtensionPoint
public interface NamespaceStrategy {

  /**
   * Create new namespace for the given repository.
   */
  String createNamespace(Repository repository);

  /**
   * Checks if the namespace can be changed when using this namespace strategy
   *
   * @return namespace can be changed
   */
  default boolean canBeChanged() {
    return false;
  }
}
