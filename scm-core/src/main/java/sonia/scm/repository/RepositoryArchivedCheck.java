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

/**
 * Implementations of this class can be used to check whether a repository is archived.
 *
 * @since 2.12.0
 */
public interface RepositoryArchivedCheck extends ReadOnlyCheck {

  boolean isArchived(String repositoryId);

  /**
   * Checks whether the given repository is archived or not. This checks the status on behalf of the id of the
   * repository, not by the archive flag provided by the repository itself.
   */
  default boolean isArchived(Repository repository) {
    return isArchived(repository.getId());
  }

  @Override
  default boolean isReadOnly(String repositoryId) {
    return isArchived(repositoryId);
  }

  @Override
  default String getReason() {
    return "repository is archived";
  }

  @Override
  default void check(Repository repository) {
    if (repository.isArchived() || isArchived(repository)) {
      throw new RepositoryArchivedException(repository);
    }
  }

  @Override
  default void check(String repositoryId) {
    if (isArchived(repositoryId)) {
      throw new RepositoryArchivedException(repositoryId);
    }
  }
}
