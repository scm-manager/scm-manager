package sonia.scm.repository;

import sonia.scm.repository.spi.SyncAsyncExecutor;
import sonia.scm.repository.spi.SyncAsyncExecutorProvider;

import java.io.Closeable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DefaultSyncAsyncExecutorProvider implements SyncAsyncExecutorProvider, Closeable {

  private final ExecutorService executor;

  public DefaultSyncAsyncExecutorProvider() {
    this(Executors.newFixedThreadPool(4));
  }

  public DefaultSyncAsyncExecutorProvider(ExecutorService executor) {
    this.executor = executor;
  }

  public SyncAsyncExecutor createExecutorWithSecondsToTimeout(int seconds) {
    return new DefaultSyncAsyncExecutor(executor, Instant.now().plus(seconds, ChronoUnit.SECONDS));
  }

  @Override
  public void close() {
    executor.shutdownNow();
  }
}
