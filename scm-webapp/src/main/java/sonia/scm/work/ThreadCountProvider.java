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
