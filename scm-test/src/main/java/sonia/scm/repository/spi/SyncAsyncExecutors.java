package sonia.scm.repository.spi;

import java.time.Instant;

public final class SyncAsyncExecutors {

  public static SyncAsyncExecutor synchronousExecutor() {
    return new SyncAsyncExecutor(Runnable::run, Instant.MAX);
  }
}
