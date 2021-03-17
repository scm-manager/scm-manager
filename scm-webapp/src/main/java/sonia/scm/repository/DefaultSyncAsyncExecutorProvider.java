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
import sonia.scm.metrics.Metrics;
import sonia.scm.repository.spi.SyncAsyncExecutor;
import sonia.scm.repository.spi.SyncAsyncExecutorProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.Closeable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
public class DefaultSyncAsyncExecutorProvider implements SyncAsyncExecutorProvider, Closeable {

  public static final int DEFAULT_MAX_ASYNC_ABORT_SECONDS = 60;
  public static final String MAX_ASYNC_ABORT_SECONDS_PROPERTY = "scm.maxAsyncAbortSeconds";

  public static final int DEFAULT_NUMBER_OF_THREADS = 4;
  public static final String NUMBER_OF_THREADS_PROPERTY = "scm.asyncThreads";

  private final ExecutorService executor;
  private final int defaultMaxAsyncAbortSeconds;

  @Inject
  public DefaultSyncAsyncExecutorProvider(MeterRegistry registry) {
    this(createExecutorService(registry, getProperty(NUMBER_OF_THREADS_PROPERTY, DEFAULT_NUMBER_OF_THREADS)));
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

  public DefaultSyncAsyncExecutorProvider(ExecutorService executor) {
    this.executor = executor;
    this.defaultMaxAsyncAbortSeconds = getProperty(MAX_ASYNC_ABORT_SECONDS_PROPERTY, DEFAULT_MAX_ASYNC_ABORT_SECONDS);
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

  private static int getProperty(String key, int defaultValue) {
    return Integer.parseInt(System.getProperty(key, Integer.toString(defaultValue)));
  }
}
