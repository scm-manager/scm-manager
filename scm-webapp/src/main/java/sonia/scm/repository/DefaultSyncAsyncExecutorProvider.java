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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sonia.scm.config.ConfigValue;
import sonia.scm.metrics.Metrics;
import sonia.scm.repository.spi.SyncAsyncExecutor;
import sonia.scm.repository.spi.SyncAsyncExecutorProvider;

import java.io.Closeable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
public class DefaultSyncAsyncExecutorProvider implements SyncAsyncExecutorProvider, Closeable {

  private final ExecutorService executor;
  private final int defaultMaxAsyncAbortSeconds;

  @Inject
  public DefaultSyncAsyncExecutorProvider(
    @ConfigValue(key="asyncThreads", defaultValue = "4", description = "Amount of async threads for execution") Integer asyncThreads,
    @ConfigValue(key="maxAsyncAbortSeconds", defaultValue = "60", description = "Max seconds for an asynchronous execution before abort") Integer maxAsyncAbortSeconds,
    MeterRegistry registry
  ) {
    this(createExecutorService(registry, asyncThreads), maxAsyncAbortSeconds);
  }

  private static ExecutorService createExecutorService(MeterRegistry registry, int fixed) {
    ExecutorService executorService = Executors.newFixedThreadPool(
      fixed,
      new ThreadFactoryBuilder()
        .setNameFormat("SyncAsyncExecutorProvider-%d")
        .build()
    );
    Metrics.executor(registry, executorService, "SyncAsyncExecutorProvider", "fixed");
    return executorService;
  }

  public DefaultSyncAsyncExecutorProvider(ExecutorService executor, Integer maxAsyncAbortSeconds) {
    this.executor = executor;
    this.defaultMaxAsyncAbortSeconds = maxAsyncAbortSeconds;
  }

  public SyncAsyncExecutor createExecutorWithSecondsToTimeout(int switchToAsyncInSeconds) {
    return createExecutorWithSecondsToTimeout(switchToAsyncInSeconds, defaultMaxAsyncAbortSeconds);
  }

  public SyncAsyncExecutor createExecutorWithSecondsToTimeout(int switchToAsyncInSeconds, int maxAsyncAbortSeconds) {
    return new DefaultSyncAsyncExecutor(
      executor,
      Instant.now().plus(switchToAsyncInSeconds, ChronoUnit.SECONDS),
      maxAsyncAbortSeconds);
  }

  @Override
  public void close() {
    executor.shutdownNow();
  }
}
