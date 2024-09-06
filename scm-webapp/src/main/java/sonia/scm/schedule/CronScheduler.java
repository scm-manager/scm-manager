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

package sonia.scm.schedule;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.metrics.Metrics;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Singleton
public class CronScheduler implements Scheduler {

  private static final Logger LOG = LoggerFactory.getLogger(CronScheduler.class);

  private final ScheduledExecutorService executorService;
  private final CronTaskFactory taskFactory;
  private final CronThreadFactory threadFactory;

  @Inject
  public CronScheduler(CronTaskFactory taskFactory, MeterRegistry registry) {
    this.taskFactory = taskFactory;
    this.threadFactory = new CronThreadFactory();
    this.executorService = createExecutor(registry);
  }

  private ScheduledExecutorService createExecutor(MeterRegistry registry) {
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(2, threadFactory);
    Metrics.executor(registry, executor, "Cron", "scheduled");
    return executor;
  }

  @Override
  public CronTask schedule(String expression, Runnable runnable) {
    return schedule(taskFactory.create(expression, runnable));
  }

  @Override
  public CronTask schedule(String expression, Class<? extends Runnable> runnable) {
    return schedule(taskFactory.create(expression, runnable));
  }

  private CronTask schedule(CronTask task) {
    if (task.hasNextRun()) {
      LOG.debug("schedule task {}", task);
      Future<?> future = executorService.scheduleAtFixedRate(task, 0L, 1L, TimeUnit.SECONDS);
      task.setFuture(future);
    } else {
      LOG.debug("skip scheduling, because task {} has no next run", task);
    }
    return task;
  }

  @Override
  public void close() {
    LOG.debug("shutdown underlying executor service");
    threadFactory.close();
    executorService.shutdown();
  }
}
