package sonia.scm.repository.spi;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SyncAsyncExecutorProvider {

  private static final int DEFAULT_TIMEOUT_SECONDS = 2;

  private final Executor executor;

  public SyncAsyncExecutorProvider() {
    this(Executors.newFixedThreadPool(4));
  }

  public SyncAsyncExecutorProvider(Executor executor) {
    this.executor = executor;
  }

  public SyncAsyncExecutor createExecutorWithDefaultTimeout() {
    return createExecutorWithSecondsToTimeout(DEFAULT_TIMEOUT_SECONDS);
  }

  public SyncAsyncExecutor createExecutorWithSecondsToTimeout(int seconds) {
    return new SyncAsyncExecutor(executor, Instant.now().plus(seconds, ChronoUnit.SECONDS));
  }
}
