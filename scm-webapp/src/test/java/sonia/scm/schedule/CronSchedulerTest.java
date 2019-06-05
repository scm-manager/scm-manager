package sonia.scm.schedule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;
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
    try (CronScheduler scheduler = new CronScheduler(taskFactory)) {
      scheduler.schedule("vep", TestingRunnable.class);
      verify(task).setFuture(any(Future.class));
    }
  }

  @Test
  void shouldScheduleWithRunnable() {
    when(task.hasNextRun()).thenReturn(true);
    try (CronScheduler scheduler = new CronScheduler(taskFactory)) {
      scheduler.schedule("vep", new TestingRunnable());
      verify(task).setFuture(any(Future.class));
    }
  }

  @Test
  void shouldSkipSchedulingWithoutNextRun(){
    try (CronScheduler scheduler = new CronScheduler(taskFactory)) {
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
