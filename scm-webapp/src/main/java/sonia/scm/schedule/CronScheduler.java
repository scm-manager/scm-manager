package sonia.scm.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Singleton
public class CronScheduler implements Scheduler {

  private static final Logger LOG = LoggerFactory.getLogger(CronScheduler.class);

  private final ScheduledExecutorService executorService;
  private final CronTaskFactory taskFactory;

  @Inject
  public CronScheduler(CronTaskFactory taskFactory) {
    this.taskFactory = taskFactory;
    this.executorService = createExecutor();
  }

  private ScheduledExecutorService createExecutor() {
    return Executors.newScheduledThreadPool(2, new CronThreadFactory());
  }

  @Override
  public CronTask schedule(String expression, Runnable runnable) {
    return schedule(taskFactory.create(expression, runnable));
  }

  @Override
  public CronTask schedule(String expression, Class<? extends Runnable> runnable) {
    return schedule(taskFactory.create(expression, runnable));
  }

  private CronTask schedule(CronTask task) {
    if (task.hasNextRun()) {
      LOG.debug("schedule task {}", task);
      Future<?> future = executorService.scheduleAtFixedRate(task, 0L, 1L, TimeUnit.SECONDS);
      task.setFuture(future);
    } else {
      LOG.debug("skip scheduling, because task {} has no next run", task);
    }
    return task;
  }

  @Override
  public void close() {
    LOG.debug("shutdown underlying executor service");
    executorService.shutdown();
  }
}
