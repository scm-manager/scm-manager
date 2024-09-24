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

package sonia.scm.work;

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.IntSupplier;

public class ThreadCountProvider implements IntSupplier {

  private static final Logger LOG = LoggerFactory.getLogger(ThreadCountProvider.class);

  private final IntSupplier cpuCountProvider;
  private final int workers;

  public ThreadCountProvider(Integer workers) {
    this(() -> Runtime.getRuntime().availableProcessors(), workers);
  }

  @VisibleForTesting
  ThreadCountProvider(IntSupplier cpuCountProvider, Integer workers) {
    this.cpuCountProvider = cpuCountProvider;
    this.workers = sanitizeWorkerCount(workers);
  }

  @Override
  public int getAsInt() {
    return workers;
  }

  private int sanitizeWorkerCount(Integer workers) {
    if (workers == null || workers == 0) {
      return deriveFromCPUCount();
    } else if (isInvalid(workers)) {
      LOG.warn(
        "config value 'centralWorkQueue.workers' contains an invalid value {}, fall back and derive worker count from cpu count", workers
      );
      return deriveFromCPUCount();
    }
    LOG.debug("using {} workers for central work queue", workers);
    return workers;
  }

  private boolean isInvalid(int value) {
    return value <= 0 || value > 64;
  }

  private int deriveFromCPUCount() {
    int cpus = cpuCountProvider.getAsInt();
    if (cpus > 1) {
      LOG.debug("using 4 workers for central work queue");
      return 4;
    }
    LOG.debug("using 2 workers for central work queue");
    return 2;
  }
}
