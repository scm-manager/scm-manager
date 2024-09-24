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
 * Manages namespaces. Mind that namespaces do not have a lifecycle on their own, but only do exist through
 * repositories. Therefore you cannot create or delete namespaces, but just change related settings like permissions
 * associated with them.
 *
 * @since 2.6.0
 */
public interface NamespaceManager {

  /**
   * Returns {@link Optional} with the namespace for the given name, or an empty {@link Optional} if there is no such
   * namespace (that is, there is no repository with this namespace).
   */
  Optional<Namespace> get(String namespace);

  /**
   * Returns a {@link java.util.Collection} of all namespaces.
   */
  Collection<Namespace> getAll();

  /**
   * Modifies the given namespace.
   */
  void modify(Namespace namespace);
}
