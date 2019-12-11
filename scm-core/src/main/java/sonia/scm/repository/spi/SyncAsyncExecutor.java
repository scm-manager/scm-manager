package sonia.scm.repository.spi;

import java.util.function.Consumer;

public interface SyncAsyncExecutor {

  default ExecutionType execute(Runnable runnable) {
    return execute(ignored -> runnable.run());
  }

  ExecutionType execute(Consumer<ExecutionType> runnable);

  boolean hasExecutedAllSynchronously();

  enum ExecutionType {
    SYNCHRONOUS, ASYNCHRONOUS
  }
}
