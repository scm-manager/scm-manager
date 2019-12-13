package sonia.scm.repository;

import sonia.scm.repository.spi.SyncAsyncExecutor;
import sonia.scm.repository.spi.SyncAsyncExecutorProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.Closeable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
public class DefaultSyncAsyncExecutorProvider implements SyncAsyncExecutorProvider, Closeable {

  public static final int DEFAULT_MAX_ASYNC_ABORT_SECONDS = 60;
  public static final String MAX_ASYNC_ABORT_SECONDS_PROPERTY = "scm.maxAsyncAbortSeconds";

  public static final int DEFAULT_NUMBER_OF_THREADS = 4;
  public static final String NUMBER_OF_THREADS_PROPERTY = "scm.asyncThreads";

  private final ExecutorService executor;
  private final int defaultMaxAsyncAbortSeconds;

  @Inject
  public DefaultSyncAsyncExecutorProvider() {
    this(Executors.newFixedThreadPool(getProperty(NUMBER_OF_THREADS_PROPERTY, DEFAULT_NUMBER_OF_THREADS)));
  }

  public DefaultSyncAsyncExecutorProvider(ExecutorService executor) {
    this.executor = executor;
    this.defaultMaxAsyncAbortSeconds = getProperty(MAX_ASYNC_ABORT_SECONDS_PROPERTY, DEFAULT_MAX_ASYNC_ABORT_SECONDS);
  }

  public SyncAsyncExecutor createExecutorWithSecondsToTimeout(int switchToAsyncInSeconds) {
    return createExecutorWithSecondsToTimeout(switchToAsyncInSeconds, defaultMaxAsyncAbortSeconds);
  }

  public SyncAsyncExecutor createExecutorWithSecondsToTimeout(int switchToAsyncInSeconds, int maxAsyncAbortSeconds) {
    return new DefaultSyncAsyncExecutor(
      executor,
      Instant.now().plus(switchToAsyncInSeconds, ChronoUnit.SECONDS),
      maxAsyncAbortSeconds);
  }

  @Override
  public void close() {
    executor.shutdownNow();
  }

  private static int getProperty(String key, int defaultValue) {
    return Integer.parseInt(System.getProperty(key, Integer.toString(defaultValue)));
  }
}
