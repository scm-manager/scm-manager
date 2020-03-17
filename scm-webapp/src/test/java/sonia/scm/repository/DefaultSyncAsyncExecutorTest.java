/**
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package sonia.scm.repository;

import org.junit.jupiter.api.Test;
import sonia.scm.repository.spi.SyncAsyncExecutor.ExecutionType;

import java.time.Instant;

import static java.lang.Integer.MAX_VALUE;
import static java.time.Instant.MAX;
import static java.time.temporal.ChronoUnit.MILLIS;
import static org.assertj.core.api.Assertions.assertThat;
import static sonia.scm.repository.spi.SyncAsyncExecutor.ExecutionType.ASYNCHRONOUS;
import static sonia.scm.repository.spi.SyncAsyncExecutor.ExecutionType.SYNCHRONOUS;

class DefaultSyncAsyncExecutorTest {

  ExecutionType calledWithType = null;
  boolean aborted = false;

  @Test
  void shouldExecuteSynchronouslyBeforeTimeout() {
    DefaultSyncAsyncExecutor executor = new DefaultSyncAsyncExecutor(Runnable::run, MAX, MAX_VALUE);

    ExecutionType result = executor.execute(type -> calledWithType = type, () -> aborted = true);

    assertThat(result).isEqualTo(SYNCHRONOUS);
    assertThat(calledWithType).isEqualTo(SYNCHRONOUS);
    assertThat(executor.hasExecutedAllSynchronously()).isTrue();
    assertThat(aborted).isFalse();
  }

  @Test
  void shouldExecuteAsynchronouslyAfterTimeout() {
    DefaultSyncAsyncExecutor executor = new DefaultSyncAsyncExecutor(Runnable::run, Instant.now().minus(1, MILLIS), MAX_VALUE);

    ExecutionType result = executor.execute(type -> calledWithType = type, () -> aborted = true);

    assertThat(result).isEqualTo(ASYNCHRONOUS);
    assertThat(calledWithType).isEqualTo(ASYNCHRONOUS);
    assertThat(executor.hasExecutedAllSynchronously()).isFalse();
    assertThat(aborted).isFalse();
  }

  @Test
  void shouldCallFallbackAfterAbortion() {
    DefaultSyncAsyncExecutor executor = new DefaultSyncAsyncExecutor(Runnable::run, Instant.now().minus(1, MILLIS), 0);

    ExecutionType result = executor.execute(type -> calledWithType = type, () -> aborted = true);

    assertThat(result).isEqualTo(ASYNCHRONOUS);
    assertThat(calledWithType).isNull();
    assertThat(aborted).isTrue();
  }
}
