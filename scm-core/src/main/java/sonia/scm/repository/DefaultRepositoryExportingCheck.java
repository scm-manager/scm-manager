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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Default implementation of {@link RepositoryExportingCheck}. This tracks the exporting status of repositories.
 */
public final class DefaultRepositoryExportingCheck implements RepositoryExportingCheck {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultRepositoryExportingCheck.class);
  private static final Map<String, AtomicInteger> EXPORTING_REPOSITORIES = Collections.synchronizedMap(new HashMap<>());

  public static boolean isRepositoryExporting(String repositoryId) {
    return getLockCount(repositoryId).get() > 0;
  }

  @Override
  public boolean isExporting(String repositoryId) {
    return isRepositoryExporting(repositoryId);
  }

  @Override
  public <T> T withExportingLock(Repository repository, Supplier<T> callback) {
    try {
      getLockCount(repository.getId()).incrementAndGet();
      return callback.get();
    } finally {
      int lockCount = getLockCount(repository.getId()).decrementAndGet();
      if (lockCount <= 0) {
        LOG.warn("Got negative export lock count {} for repository {}", lockCount, repository);
        EXPORTING_REPOSITORIES.remove(repository.getId());
      }
    }
  }

  private static AtomicInteger getLockCount(String repositoryId) {
    return EXPORTING_REPOSITORIES.computeIfAbsent(repositoryId, r -> new AtomicInteger(0));
  }
}
