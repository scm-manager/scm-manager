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

import static sonia.scm.ContextEntry.ContextBuilder.entity;

/**
 * Read only check could be used to mark a repository as read only.
 * @since 2.19.0
 */
@ExtensionPoint
public interface ReadOnlyCheck {

  /**
   * Returns the reason for the write protection.
   */
  String getReason();

  /**
   * Returns {@code true} if the repository with the given id is read only. If this is the case, all permissions not
   * marked "read only" will be denied fot this repository, stores for this repository cannot be written, and calling
   * modifying commands will be prevented. If only special permissions should be forbidden without blocking stores
   * and commands, use {@link #isForbidden(String, String)} instead.
   *
   * @param repositoryId repository id
   * @return {@code true} if repository is read only
   * @see #isForbidden(String, String)
   */
  default boolean isReadOnly(String repositoryId) {
    return false;
  }

  /**
   * Returns {@code true} if the repository is read only. By default forwards to {@link #isReadOnly(String)} with the
   * id of the given repository.
   *
   * @param repository The repository to check for.
   * @return {@code true} if repository is read only
   * @see #isReadOnly(String)
   */
  default boolean isReadOnly(Repository repository) {
    return isReadOnly(repository.getId());
  }

  /**
   * Throws a {@link ReadOnlyException} if the repository is read only.
   * @param repository The repository to check for.
   */
  default void check(Repository repository) {
    check(repository.getId());
  }

  /**
   * Throws a {@link ReadOnlyException} if the repository with th id is read only.
   * @param repositoryId The id of the repository to check for.
   */
  default void check(String repositoryId) {
    if (isReadOnly(repositoryId)) {
      throw new ReadOnlyException(entity(Repository.class, repositoryId).build(), getReason());
    }
  }

  /**
   * By default returns {@link #isReadOnly(String)}. This is meant to be overridden if only specific permissions
   * should be blocked. In contrast to {@link #isReadOnly(String)}, writing to stores or calling modifying commands
   * will not be prevented, so this relies on permission checks only.
   *
   * @param permission   The permission to check.
   * @param repositoryId The id of the repository to check for.
   * @return {@code true} if this permission should be forbidden for the current repository regardless of the
   * current user.
   */
  default boolean isForbidden(String permission, String repositoryId) {
    return isReadOnly(repositoryId);
  }

  /**
   * @deprecated This method is named badly. Please use {@link #isForbidden(String, String)} instead. This
   * implementation simply delegates to this method.
   */
  @Deprecated
  default boolean isReadOnly(String permission, String repositoryId) {
    return isForbidden(permission, repositoryId);
  }
}
