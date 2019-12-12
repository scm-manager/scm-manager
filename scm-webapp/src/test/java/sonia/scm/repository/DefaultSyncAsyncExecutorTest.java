package sonia.scm.repository;

import org.junit.jupiter.api.Test;
import sonia.scm.repository.spi.SyncAsyncExecutor.ExecutionType;

import java.time.Instant;

import static java.time.temporal.ChronoUnit.MILLIS;
import static org.assertj.core.api.Assertions.assertThat;
import static sonia.scm.repository.spi.SyncAsyncExecutor.ExecutionType.ASYNCHRONOUS;
import static sonia.scm.repository.spi.SyncAsyncExecutor.ExecutionType.SYNCHRONOUS;

class DefaultSyncAsyncExecutorTest {

  ExecutionType calledWithType = null;
  boolean aborted = false;

  @Test
  void shouldExecuteSynchronouslyBeforeTimeout() {
    DefaultSyncAsyncExecutor executor = new DefaultSyncAsyncExecutor(Runnable::run, Instant.MAX);

    ExecutionType result = executor.execute(type -> calledWithType = type, () -> aborted = true);

    assertThat(result).isEqualTo(SYNCHRONOUS);
    assertThat(calledWithType).isEqualTo(SYNCHRONOUS);
    assertThat(executor.hasExecutedAllSynchronously()).isTrue();
    assertThat(aborted).isFalse();
  }

  @Test
  void shouldExecuteAsynchronouslyAfterTimeout() {
    DefaultSyncAsyncExecutor executor = new DefaultSyncAsyncExecutor(Runnable::run, Instant.now().minus(1, MILLIS));

    ExecutionType result = executor.execute(type -> calledWithType = type, () -> aborted = true);

    assertThat(result).isEqualTo(ASYNCHRONOUS);
    assertThat(calledWithType).isEqualTo(ASYNCHRONOUS);
    assertThat(executor.hasExecutedAllSynchronously()).isFalse();
    assertThat(aborted).isFalse();
  }

  @Test
  void shouldCallFallbackAfterAbortion() {
    DefaultSyncAsyncExecutor executor = new DefaultSyncAsyncExecutor(Runnable::run, Instant.now().minus(1, MILLIS), 0L);

    ExecutionType result = executor.execute(type -> calledWithType = type, () -> aborted = true);

    assertThat(result).isEqualTo(ASYNCHRONOUS);
    assertThat(calledWithType).isNull();
    assertThat(aborted).isTrue();
  }
}
