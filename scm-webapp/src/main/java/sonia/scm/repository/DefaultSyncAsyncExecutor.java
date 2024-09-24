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

import sonia.scm.repository.spi.SyncAsyncExecutor;

import java.time.Instant;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static sonia.scm.repository.spi.SyncAsyncExecutor.ExecutionType.ASYNCHRONOUS;
import static sonia.scm.repository.spi.SyncAsyncExecutor.ExecutionType.SYNCHRONOUS;

public class DefaultSyncAsyncExecutor implements SyncAsyncExecutor {

  private final Executor executor;
  private final Instant switchToAsyncTime;
  private final long maxAsyncAbortMilliseconds;
  private AtomicLong accumulatedAsyncRuntime = new AtomicLong(0L);
  private boolean executedAllSynchronously = true;

  DefaultSyncAsyncExecutor(Executor executor, Instant switchToAsyncTime, int maxAsyncAbortSeconds) {
    this.executor = executor;
    this.switchToAsyncTime = switchToAsyncTime;
    this.maxAsyncAbortMilliseconds = maxAsyncAbortSeconds * 1000L;
  }

  public ExecutionType execute(Consumer<ExecutionType> task, Runnable abortionFallback) {
    if (switchToAsyncTime.isBefore(Instant.now())) {
      executor.execute(() -> {
        if (accumulatedAsyncRuntime.get() < maxAsyncAbortMilliseconds) {
          long chunkStartTime = System.currentTimeMillis();
          task.accept(ASYNCHRONOUS);
          accumulatedAsyncRuntime.addAndGet(System.currentTimeMillis() - chunkStartTime);
        } else {
          abortionFallback.run();
        }
      });
      executedAllSynchronously = false;
      return ASYNCHRONOUS;
    } else {
      task.accept(SYNCHRONOUS);
      return SYNCHRONOUS;
    }
  }

  public boolean hasExecutedAllSynchronously() {
    return executedAllSynchronously;
  }
}
