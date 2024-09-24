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

import java.util.function.Supplier;

/**
 * Implementations of this class can be used to check whether a repository is currently being exported.
 *
 * @since 2.14.0
 */
public interface RepositoryExportingCheck extends ReadOnlyCheck {

  /**
   * Checks whether the repository with the given id is currently (that is, at this moment) being exported or not.
   * @param repositoryId The id of the repository to check.
   * @return <code>true</code> when the repository with the given id is currently being exported, <code>false</code>
   * otherwise.
   */
  boolean isExporting(String repositoryId);

  /**
   * Checks whether the given repository is currently (that is, at this moment) being exported or not. This checks the
   * status on behalf of the id of the repository, not by the exporting flag provided by the repository itself.
   * @param repository The repository to check.
   * @return <code>true</code> when the given repository is currently being exported, <code>false</code> otherwise.
   */
  default boolean isExporting(Repository repository) {
    return isExporting(repository.getId());
  }

  /**
   * Asserts that the given repository is marked as being exported during the execution of the given callback.
   * @param repository The repository that will be marked as being exported.
   * @param callback This callback will be executed.
   * @param <T> The return type of the callback.
   * @return The result of the callback.
   */
  <T> T withExportingLock(Repository repository, Supplier<T> callback);

  @Override
  default boolean isReadOnly(String repositoryId) {
    return isExporting(repositoryId);
  }

  @Override
  default String getReason() {
    return "repository is exporting";
  }

  @Override
  default void check(String repositoryId) {
    if (isExporting(repositoryId)) {
      throw new RepositoryExportingException(repositoryId);
    }
  }
}
