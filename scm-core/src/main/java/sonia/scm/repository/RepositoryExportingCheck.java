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

import java.util.function.Supplier;

/**
 * Implementations of this class can be used to check whether a repository is currently being exported.
 *
 * @since 2.14.0
 */
public interface RepositoryExportingCheck {

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
}
