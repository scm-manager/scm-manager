package sonia.scm.repository.spi;

import java.util.function.Consumer;

public interface SyncAsyncExecutor {

  default ExecutionType execute(Runnable runnable) {
    return execute(ignored -> runnable.run(), () -> {});
  }

  default ExecutionType execute(Runnable runnable, Runnable abortionFallback) {
    return execute(ignored -> runnable.run(), abortionFallback);
  }

  ExecutionType execute(Consumer<ExecutionType> runnable, Runnable abortionFallback);

  boolean hasExecutedAllSynchronously();

  enum ExecutionType {
    SYNCHRONOUS, ASYNCHRONOUS
  }
}
