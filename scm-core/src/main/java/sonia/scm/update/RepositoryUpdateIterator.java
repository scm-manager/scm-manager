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
 * Implementations of this interface can be used to iterate all repositories in update steps.
 *
 * @since 2.13.0
 */
public interface RepositoryUpdateIterator {

  /**
   * Calls the given consumer with each repository id.
   *
   * @since 2.13.0
   */
  void forEachRepository(Consumer<String> repositoryIdConsumer);

  /**
   * Equivalent to {@link #forEachRepository(Consumer)} with the difference, that you can throw exceptions in the given
   * update code, that will then be wrapped in a {@link UpdateException}.
   *
   * @since 2.14.0
   */
  default void updateEachRepository(Updater updater) {
    forEachRepository(
      repositoryId -> {
        try {
          updater.update(repositoryId);
        } catch (Exception e) {
          throw new UpdateException("failed to update repository with id " + repositoryId, e);
        }
      }
    );
  }

  /**
   * Simple callback with the id of an existing repository with the possibility to throw exceptions.
   *
   * @since 2.14.0
   */
  interface Updater {
    /**
     * Implements the update logic for a single repository, denoted by its id.
     */
    @SuppressWarnings("java:S112") // We explicitly want to allow arbitrary exceptions here
    void update(String repositoryId) throws Exception;
  }
}
