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
package sonia.scm.schedule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.Future;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CronTaskTest {

  @Mock
  private CronExpression expression;

  @Mock
  private Runnable runnable;

  @Mock
  private Future<?> future;

  @Test
  void shouldReturnTrue() {
    when(expression.calculateNextRun()).thenReturn(Optional.of(ZonedDateTime.now()));

    CronTask task = task();

    assertThat(task.hasNextRun()).isTrue();
  }

  @Test
  void shouldReturnFalse() {
    when(expression.calculateNextRun()).thenReturn(Optional.empty());

    CronTask task = task();

    assertThat(task.hasNextRun()).isFalse();
  }

  private CronTask task() {
    return new CronTask("one", expression, runnable);
  }

  @Test
  void shouldCancelWithoutNextRun() {
    ZonedDateTime time = ZonedDateTime.now();
    when(expression.calculateNextRun()).thenReturn(Optional.of(time), Optional.empty());
    when(expression.shouldRun(time)).thenReturn(true);

    CronTask task = task();
    task.setFuture(future);
    task.run();

    verify(runnable).run();
    verify(future).cancel(false);
  }

  @Test
  void shouldNotRunAfterCancelHasBeenCalledIfRunIsCalledAgain() {
    ZonedDateTime time = ZonedDateTime.now();
    when(expression.calculateNextRun()).thenReturn(Optional.of(time), Optional.empty());
    when(expression.shouldRun(time)).thenReturn(true);

    CronTask task = task();
    task.setFuture(future);

    task.run();
    task.run();

    verify(future).cancel(false);
    verify(runnable).run();
  }

  @Test
  void shouldNotRun() {
    task().run();

    verify(runnable, never()).run();
  }
}
