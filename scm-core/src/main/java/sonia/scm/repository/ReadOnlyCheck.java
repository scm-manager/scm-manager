/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
