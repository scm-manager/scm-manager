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
