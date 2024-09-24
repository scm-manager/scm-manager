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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.Extension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Default implementation of {@link RepositoryExportingCheck}. This tracks the exporting status of repositories.
 */
@Extension
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
