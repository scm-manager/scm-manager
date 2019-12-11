package sonia.scm.repository.spi;

public interface SyncAsyncExecutorProvider {

  int DEFAULT_TIMEOUT_SECONDS = 2;

  default SyncAsyncExecutor createExecutorWithDefaultTimeout() {
    return createExecutorWithSecondsToTimeout(DEFAULT_TIMEOUT_SECONDS);
  }

  SyncAsyncExecutor createExecutorWithSecondsToTimeout(int seconds);
}
