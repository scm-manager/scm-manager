package sonia.scm.lifecycle;

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.event.RecreateEventBusEvent;
import sonia.scm.event.ScmEventBus;
import sonia.scm.event.ShutdownEventBusEvent;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Restart strategy which tries to free, every resource used by the context, starts gc and re initializes the context.
 * <strong>Warning: </strong> This strategy should only be used with an classloader lifecycle which protects the
 * created plugin classloader from classloader leaks.
 */
class InjectionContextRestartStrategy implements RestartStrategy {

  static final String NAME = "context";

  private static final String DISABLE_RESTART_PROPERTY = "sonia.scm.restart.disable";
  private static final String WAIT_PROPERTY = "sonia.scm.restart.wait";
  private static final String DISABLE_GC_PROPERTY = "sonia.scm.restart.disable-gc";

  private static final AtomicLong INSTANCE_COUNTER = new AtomicLong();
  private static final Logger LOG = LoggerFactory.getLogger(InjectionContextRestartStrategy.class);

  private boolean restartEnabled = !Boolean.getBoolean(DISABLE_RESTART_PROPERTY);
  private long waitInMs = Integer.getInteger(WAIT_PROPERTY, 250);
  private boolean gcEnabled = !Boolean.getBoolean(DISABLE_GC_PROPERTY);

  private final ClassLoader webAppClassLoader;

  InjectionContextRestartStrategy(ClassLoader webAppClassLoader) {
    this.webAppClassLoader = webAppClassLoader;
  }

  @VisibleForTesting
  void setWaitInMs(long waitInMs) {
    this.waitInMs = waitInMs;
  }

  @VisibleForTesting
  void setGcEnabled(boolean gcEnabled) {
    this.gcEnabled = gcEnabled;
  }

  @Override
  public void restart(InjectionContext context) {
    stop(context);
    if (restartEnabled) {
      start(context);
    } else {
      LOG.warn("restarting context is disabled");
    }
  }

  @SuppressWarnings("squid:S1215") // suppress explicit gc call warning
  private void start(InjectionContext context) {
    LOG.debug("use WebAppClassLoader as ContextClassLoader, to avoid ClassLoader leaks");
    Thread.currentThread().setContextClassLoader(webAppClassLoader);

    LOG.warn("send recreate eventbus event");
    ScmEventBus.getInstance().post(new RecreateEventBusEvent());

    // restart context delayed, to avoid timing problems
    new Thread(() -> {
      try {
        if (gcEnabled){
          LOG.info("call gc to clean up memory from old instances");
          System.gc();
        }

        LOG.info("wait {}ms before re starting the context", waitInMs);
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

  private void stop(InjectionContext context) {
    LOG.warn("destroy injection context");
    context.destroy();

    if (!restartEnabled) {
      // shutdown eventbus, but do this only if restart is disabled
      ScmEventBus.getInstance().post(new ShutdownEventBusEvent());
    }
  }
}
