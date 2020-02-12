package sonia.scm.lifecycle;

import com.google.common.annotations.VisibleForTesting;
import sonia.scm.event.ScmEventBus;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DefaultRestarter implements Restarter {

  private ScmEventBus eventBus;
  private RestartStrategy strategy;

  @Inject
  public DefaultRestarter() {
    this(
      ScmEventBus.getInstance(),
      RestartStrategy.get(Thread.currentThread().getContextClassLoader()).orElse(null)
    );
  }

  @VisibleForTesting
  DefaultRestarter(ScmEventBus eventBus, RestartStrategy strategy) {
    this.eventBus = eventBus;
    this.strategy = strategy;
  }

  @Override
  public boolean isSupported() {
    return strategy != null;
  }

  @Override
  public void restart(Class<?> cause, String reason) {
    if (!isSupported()) {
      throw new RestartNotSupportedException("restarting is not supported");
    }
    eventBus.post(new RestartEvent(cause, reason));
  }
}
