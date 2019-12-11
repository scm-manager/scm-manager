package sonia.scm.repository.spi;

import java.util.function.Consumer;

public interface SyncAsyncExecutor {

  ExecutionType execute(Runnable runnable);

  ExecutionType execute(Consumer<ExecutionType> runnable);

  boolean hasExecutedAllSynchronously();

  enum ExecutionType {
    SYNCHRONOUS, ASYNCHRONOUS
  }
}
