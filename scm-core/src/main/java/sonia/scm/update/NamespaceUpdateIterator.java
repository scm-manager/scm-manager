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

package sonia.scm.update;

import sonia.scm.migration.UpdateException;

import java.util.function.Consumer;

/**
 * Implementations of this interface can be used to iterate all namespaces in update steps.
 *
 * @since 2.47.0
 */
public interface NamespaceUpdateIterator {

  /**
   * Calls the given consumer with each namespace.
   *
   * @since 2.47.0
   */
  void forEachNamespace(Consumer<String> namespace);

  /**
   * Equivalent to {@link #forEachNamespace(Consumer)} with the difference, that you can throw exceptions in the given
   * update code, that will then be wrapped in a {@link UpdateException}.
   *
   * @since 2.47.0
   */
  default void updateEachNamespace(Updater updater) {
    forEachNamespace(
      namespace -> {
        try {
          updater.update(namespace);
        } catch (Exception e) {
          throw new UpdateException("failed to update namespace " + namespace, e);
        }
      }
    );
  }

  /**
   * Simple callback with the name of an existing namespace with the possibility to throw exceptions.
   *
   * @since 2.47.0
   */
  interface Updater {
    /**
     * Implements the update logic for a single namespace, denoted by its name.
     */
    @SuppressWarnings("java:S112") // We explicitly want to allow arbitrary exceptions here
    void update(String namespace) throws Exception;
  }
}
