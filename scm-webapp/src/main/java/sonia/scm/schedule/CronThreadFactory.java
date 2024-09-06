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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.shiro.util.ThreadContext;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This thread factory creates threads without a shiro context.
 * This is to avoid classloader leaks, because the {@link ThreadContext} of shiro uses {@link InheritableThreadLocal},
 * which could bind a class with a reference to a {@link sonia.scm.plugin.PluginClassLoader}.
 */
class CronThreadFactory implements ThreadFactory, AutoCloseable {

  private static final String NAME_TEMPLATE = "CronScheduler-%d-%d";

  private static final AtomicLong FACTORY_COUNTER = new AtomicLong();

  private final ExecutorService executorService = Executors.newSingleThreadExecutor(
    new ThreadFactoryBuilder().setNameFormat("CronThreadFactory-%d").build()
  );

  private final long factoryId = FACTORY_COUNTER.incrementAndGet();
  private final AtomicLong threadCounter = new AtomicLong();

  @Override
  public Thread newThread(final Runnable r) {
    try {
      return executorService.submit(() -> {
        ThreadContext.remove();
        return new Thread(r, createName());
      }).get();
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("failed to schedule runnable");
    } catch (ExecutionException ex) {
      throw new IllegalStateException("failed to schedule runnable");
    }
  }

  private String createName() {
    long threadId = threadCounter.incrementAndGet();
    return String.format(NAME_TEMPLATE, factoryId, threadId);
  }

  @Override
  public void close()  {
    executorService.shutdown();
  }
}
