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
import com.google.common.base.Stopwatch;
import com.google.inject.Injector;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.EqualsAndHashCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@EqualsAndHashCode
abstract class UnitOfWork implements Runnable, Serializable, Comparable<UnitOfWork> {

  @VisibleForTesting
  static final String METRIC_EXECUTION = "cwq.task.execution.duration";

  @VisibleForTesting
  static final String METRIC_WAIT = "cwq.task.wait.duration";

  private static final Logger LOG = LoggerFactory.getLogger(UnitOfWork.class);

  private long order;
  private int blockCount = 0;
  private int restoreCount = 0;
  private final Set<Resource> locks;

  private transient Finalizer finalizer;
  private transient Task task;
  private transient MeterRegistry meterRegistry;

  private transient long createdAt;
  private transient String storageId;

  protected UnitOfWork(long order, Set<Resource> locks) {
    this.order = order;
    this.locks = locks;
    this.createdAt = System.nanoTime();
  }

  public long getOrder() {
    return order;
  }

  public void restore(long newOrderId) {
    this.order = newOrderId;
    this.createdAt = System.nanoTime();
    this.restoreCount++;
  }

  public int getRestoreCount() {
    return restoreCount;
  }

  public void blocked() {
    blockCount++;
  }

  public void assignStorageId(String storageId) {
    this.storageId = storageId;
  }

  public Optional<String> getStorageId() {
    return Optional.ofNullable(storageId);
  }

  public Set<Resource> getLocks() {
    return locks;
  }

  void init(Injector injector, Finalizer finalizer, MeterRegistry meterRegistry) {
    this.task = task(injector);
    this.finalizer = finalizer;
    this.meterRegistry = meterRegistry;
  }

  protected abstract Task task(Injector injector);

  @Override
  public void run() {
    Stopwatch sw = Stopwatch.createStarted();
    Timer.Sample sample = Timer.start(meterRegistry);
    try {
      task.run();
      LOG.debug("task {} finished successful after {}", task, sw.stop());
    } catch (Exception ex) {
      LOG.error("task {} failed after {}", task, sw.stop(), ex);
    } finally {
      sample.stop(createExecutionTimer());
      createWaitTimer().record(System.nanoTime() - createdAt, TimeUnit.NANOSECONDS);
      finalizer.finalizeWork(this);
    }
  }

  private Timer createExecutionTimer() {
    return Timer.builder(METRIC_EXECUTION)
      .description("Central work queue task execution duration")
      .tags("task", task.getClass().getName())
      .register(meterRegistry);
  }

  private Timer createWaitTimer() {
    return Timer.builder(METRIC_WAIT)
      .description("Central work queue task wait duration")
      .tags("task", task.getClass().getName(), "restores", String.valueOf(restoreCount), "blocked", String.valueOf(blockCount))
      .register(meterRegistry);
  }

  @Override
  public int compareTo(UnitOfWork o) {
    return Long.compare(order, o.order);
  }
}
