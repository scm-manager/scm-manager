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
