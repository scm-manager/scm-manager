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

import sonia.scm.EagerSingleton;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.function.Function;

/**
 * Default implementation of {@link RepositoryExportingCheck}. This tracks the exporting status of repositories.
 */
@EagerSingleton
public class DefaultRepositoryExportingCheck implements RepositoryExportingCheck, RepositoryReadOnlyCheck {

  private static final Collection<String> EXPORTING_REPOSITORIES = Collections.synchronizedSet(new HashSet<>());

  public static void setAsExporting(String repositoryId) {
    EXPORTING_REPOSITORIES.add(repositoryId);
  }

  public static void removeFromExporting(String repositoryId) {
    EXPORTING_REPOSITORIES.remove(repositoryId);
  }

  public static boolean isRepositoryExporting(String repositoryId) {
    return EXPORTING_REPOSITORIES.contains(repositoryId);
  }

  @Override
  public boolean isExporting(String repositoryId) {
    return EXPORTING_REPOSITORIES.contains(repositoryId);
  }

  @Override
  public boolean isReadOnly(String repositoryId) {
    return isRepositoryExporting(repositoryId);
  }

  public static <T> T withReadOnlyLock(Repository repository, Function<Repository, T> callback) throws IOException {
    try {
      DefaultRepositoryExportingCheck.setAsExporting(repository.getId());
      repository.setExporting(true);
      return callback.apply(repository);
    } finally {
      DefaultRepositoryExportingCheck.removeFromExporting(repository.getId());
      repository.setExporting(false);
    }
  }
}
