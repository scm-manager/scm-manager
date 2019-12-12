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
