package sonia.scm.repository.spi;

import java.util.function.Consumer;

import static sonia.scm.repository.spi.SyncAsyncExecutor.ExecutionType.SYNCHRONOUS;

public final class SyncAsyncExecutors {

  public static SyncAsyncExecutor synchronousExecutor() {
    return new SyncAsyncExecutor() {
      @Override
      public ExecutionType execute(Consumer<ExecutionType> runnable) {
        runnable.accept(SYNCHRONOUS);
        return SYNCHRONOUS;
      }

      @Override
      public boolean hasExecutedAllSynchronously() {
        return true;
      }
    };
  }
}
