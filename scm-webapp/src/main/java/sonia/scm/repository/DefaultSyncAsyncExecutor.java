package sonia.scm.repository;

import sonia.scm.repository.spi.SyncAsyncExecutor;

import java.time.Instant;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import static sonia.scm.repository.spi.SyncAsyncExecutor.ExecutionType.ASYNCHRONOUS;
import static sonia.scm.repository.spi.SyncAsyncExecutor.ExecutionType.SYNCHRONOUS;

public class DefaultSyncAsyncExecutor implements SyncAsyncExecutor {

  private final Executor executor;
  private final Instant switchToAsyncTime;
  private boolean executedAllSynchronously = true;

  DefaultSyncAsyncExecutor(Executor executor, Instant switchToAsyncTime) {
    this.executor = executor;
    this.switchToAsyncTime = switchToAsyncTime;
  }

  public ExecutionType execute(Runnable runnable) {
    return execute(ignored -> runnable.run());
  }

  public ExecutionType execute(Consumer<ExecutionType> runnable) {
    if (Instant.now().isAfter(switchToAsyncTime)) {
      executor.execute(() -> runnable.accept(ASYNCHRONOUS));
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
