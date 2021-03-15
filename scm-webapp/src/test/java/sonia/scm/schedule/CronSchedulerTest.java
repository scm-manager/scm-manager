/*
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

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.Future;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CronSchedulerTest {

  @Mock
  private CronTaskFactory taskFactory;

  @Mock
  private CronTask task;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUpTaskFactory() {
    lenient().when(taskFactory.create(anyString(), any(Runnable.class))).thenReturn(task);
    lenient().when(taskFactory.create(anyString(), any(Class.class))).thenReturn(task);
  }

  @Test
  void shouldScheduleWithClass() {
    when(task.hasNextRun()).thenReturn(true);
    try (CronScheduler scheduler = new CronScheduler(taskFactory, new SimpleMeterRegistry())) {
      scheduler.schedule("vep", TestingRunnable.class);
      verify(task).setFuture(any(Future.class));
    }
  }

  @Test
  void shouldScheduleWithRunnable() {
    when(task.hasNextRun()).thenReturn(true);
    try (CronScheduler scheduler = new CronScheduler(taskFactory, new SimpleMeterRegistry())) {
      scheduler.schedule("vep", new TestingRunnable());
      verify(task).setFuture(any(Future.class));
    }
  }

  @Test
  void shouldSkipSchedulingWithoutNextRun(){
    try (CronScheduler scheduler = new CronScheduler(taskFactory, new SimpleMeterRegistry())) {
      scheduler.schedule("vep", new TestingRunnable());
      verify(task, never()).setFuture(any(Future.class));
    }
  }

  private static class TestingRunnable implements Runnable {

    @Override
    public void run() {

    }
  }

}
