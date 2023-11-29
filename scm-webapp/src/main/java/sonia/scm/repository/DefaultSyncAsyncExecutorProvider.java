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
