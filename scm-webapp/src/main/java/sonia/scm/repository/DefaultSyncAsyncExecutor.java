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
