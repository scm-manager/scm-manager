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
import com.google.common.base.Stopwatch;
import com.google.inject.Injector;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.EqualsAndHashCode;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.security.Impersonator;
import sonia.scm.security.Impersonator.Session;

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
  private final PrincipalCollection principal;

  private transient Finalizer finalizer;
  private transient Runnable task;
  private transient MeterRegistry meterRegistry;
  private transient Impersonator impersonator;

  private transient long createdAt;
  private transient String storageId;

  protected UnitOfWork(long order, PrincipalCollection principal, Set<Resource> locks) {
    this.order = order;
    this.principal = principal;
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
    this.impersonator = injector.getInstance(Impersonator.class);
  }

  protected abstract Runnable task(Injector injector);

  @Override
  public void run() {
    Stopwatch sw = Stopwatch.createStarted();
    Timer.Sample sample = Timer.start(meterRegistry);
    try (Session session = impersonator.impersonate(principal)) {
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
