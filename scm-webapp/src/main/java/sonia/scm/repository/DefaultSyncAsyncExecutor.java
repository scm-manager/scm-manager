package sonia.scm.repository;

import sonia.scm.repository.spi.SyncAsyncExecutor;

import java.time.Instant;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static sonia.scm.repository.spi.SyncAsyncExecutor.ExecutionType.ASYNCHRONOUS;
import static sonia.scm.repository.spi.SyncAsyncExecutor.ExecutionType.SYNCHRONOUS;

public class DefaultSyncAsyncExecutor implements SyncAsyncExecutor {

  public static final long DEFAULT_MAX_ASYNC_RUNTIME = 60 * 1000L;

  private final Executor executor;
  private final Instant switchToAsyncTime;
  private final long maxAsyncRuntime;
  private AtomicLong asyncRuntime = new AtomicLong(0L);
  private boolean executedAllSynchronously = true;

  DefaultSyncAsyncExecutor(Executor executor, Instant switchToAsyncTime) {
    this(executor, switchToAsyncTime, DEFAULT_MAX_ASYNC_RUNTIME);
  }

  DefaultSyncAsyncExecutor(Executor executor, Instant switchToAsyncTime, long maxAsyncRuntime) {
    this.executor = executor;
    this.switchToAsyncTime = switchToAsyncTime;
    this.maxAsyncRuntime = maxAsyncRuntime;
  }

  public ExecutionType execute(Consumer<ExecutionType> runnable, Runnable abortionFallback) {
    if (Instant.now().isAfter(switchToAsyncTime)) {
      executor.execute(() -> {
        if (asyncRuntime.get() < maxAsyncRuntime) {
          long chunkStartTime = System.currentTimeMillis();
          runnable.accept(ASYNCHRONOUS);
          asyncRuntime.addAndGet(System.currentTimeMillis() - chunkStartTime);
        } else {
          abortionFallback.run();
        }
      });
      executedAllSynchronously = false;
      return ASYNCHRONOUS;
    } else {
      runnable.accept(SYNCHRONOUS);
      return SYNCHRONOUS;
    }
  }

  public boolean hasExecutedAllSynchronously() {
    return executedAllSynchronously;
  }
}
