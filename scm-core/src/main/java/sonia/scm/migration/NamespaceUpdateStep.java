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

package sonia.scm.migration;

import sonia.scm.plugin.ExtensionPoint;

/**
 * This is the main interface for "namespace specific" data migration/update. Using this interface, SCM-Manager
 * provides the possibility to change data structures between versions for a given type of data. This class should be
 * used only for namespace specific data (eg. the store is created with a call of <code>forNamespace</code> in a store
 * factory. To migrate global data, use a {@link UpdateStep}.
 * <p>For information about {@link #getAffectedDataType()} and {@link #getTargetVersion()}, see the package
 * documentation.</p>
 *
 * @see sonia.scm.migration
 *
 * @since 2.47.0
 */
@ExtensionPoint
public interface NamespaceUpdateStep extends UpdateStepTarget {
  /**
   * Implement this to update the data to the new version for a specific namespace. If any {@link Exception} is thrown,
   * SCM-Manager will not start up.
   *
   * @param namespaceUpdateContext A context providing specifics about the namespace, whose data should be migrated
   *                                (eg. its name).
   */
  @SuppressWarnings("java:S112") // we suppress this one, because an implementation should feel free to throw any exception it deems necessary
  void doUpdate(NamespaceUpdateContext namespaceUpdateContext) throws Exception;
}
