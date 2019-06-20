package sonia.scm.boot;

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.event.RecreateEventBusEvent;
import sonia.scm.event.ScmEventBus;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Restart strategy implementation which destroy the injection context and re initialize it.
 */
public class InjectionContextRestartStrategy implements RestartStrategy {

  private static final AtomicLong INSTANCE_COUNTER = new AtomicLong();
  private static final Logger LOG = LoggerFactory.getLogger(InjectionContextRestartStrategy.class);

  private long waitInMs = 250L;

  @VisibleForTesting
  void setWaitInMs(long waitInMs) {
    this.waitInMs = waitInMs;
  }

  @Override
  public void restart(InjectionContext context) {
    LOG.warn("destroy injection context");
    context.destroy();

    LOG.warn("send recreate eventbus event");
    ScmEventBus.getInstance().post(new RecreateEventBusEvent());

    // restart context delayed, to avoid timing problems
    new Thread(() -> {
      try {
        Thread.sleep(waitInMs);

        LOG.warn("reinitialize injection context");
        context.initialize();

        LOG.debug("register injection context on new eventbus");
        ScmEventBus.getInstance().register(context);
      } catch ( Exception ex) {
        LOG.error("failed to restart", ex);
      }
    }, "Delayed-Restart-" + INSTANCE_COUNTER.incrementAndGet()).start();

  }
}
