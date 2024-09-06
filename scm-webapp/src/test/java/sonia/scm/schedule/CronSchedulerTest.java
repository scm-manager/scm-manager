/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
