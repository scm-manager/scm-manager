package sonia.scm.schedule;

import com.google.inject.Injector;
import com.google.inject.util.Providers;

import javax.inject.Inject;
import javax.inject.Provider;

class CronTaskFactory {

  private final Injector injector;
  private final PrivilegedRunnableFactory runnableFactory;

  @Inject
  public CronTaskFactory(Injector injector, PrivilegedRunnableFactory runnableFactory) {
    this.injector = injector;
    this.runnableFactory = runnableFactory;
  }

  CronTask create(String expression, Runnable runnable) {
    return create(expression, runnable.getClass().getName(), Providers.of(runnable));
  }

  CronTask create(String expression, Class<? extends Runnable> runnable) {
    return create(expression, runnable.getName(), injector.getProvider(runnable));
  }

  private CronTask create(String expression, String name, Provider<? extends Runnable> runnableProvider) {
    Runnable runnable = runnableFactory.create(runnableProvider);
    return new CronTask(name, new CronExpression(expression), runnable);
  }
}
