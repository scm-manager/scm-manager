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

import javax.inject.Inject;

/**
 * Checks, whether a repository has to be considered read only. Currently, this includes {@link RepositoryArchivedCheck}
 * and {@link RepositoryExportingCheck}.
 *
 * @since 2.14.0
 */
public final class RepositoryReadOnlyChecker {

  private final RepositoryArchivedCheck archivedCheck;
  private final RepositoryExportingCheck exportingCheck;

  @Inject
  public RepositoryReadOnlyChecker(RepositoryArchivedCheck archivedCheck, RepositoryExportingCheck exportingCheck) {
    this.archivedCheck = archivedCheck;
    this.exportingCheck = exportingCheck;
  }

  /**
   * Checks if the repository is read only.
   * @param repository The repository to check.
   * @return <code>true</code> if any check locks the repository to read only access.
   */
  public boolean isReadOnly(Repository repository) {
    return isReadOnly(repository.getId());
  }

  /**
   * Checks if the repository for the given id is read only.
   * @param repositoryId The id of the given repository to check.
   * @return <code>true</code> if any check locks the repository to read only access.
   */
  public boolean isReadOnly(String repositoryId) {
    return archivedCheck.isArchived(repositoryId) || exportingCheck.isExporting(repositoryId);
  }

  /**
   * Checks if the repository may be modified.
   *
   * @throws RepositoryArchivedException  if the repository is archived
   * @throws RepositoryExportingException if the repository is currently being exported
   */
  public static void checkReadOnly(Repository repository) {
    if (isArchived(repository)) {
      throw new RepositoryArchivedException(repository);
    }
    if (isExporting(repository)) {
      throw new RepositoryExportingException(repository);
    }
  }

  private static boolean isExporting(Repository repository) {
    return DefaultRepositoryExportingCheck.isRepositoryExporting(repository.getId());
  }

  private static boolean isArchived(Repository repository) {
    return repository.isArchived() || EventDrivenRepositoryArchiveCheck.isRepositoryArchived(repository.getId());
  }
}
