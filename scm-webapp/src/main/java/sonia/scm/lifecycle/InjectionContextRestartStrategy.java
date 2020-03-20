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
    
package sonia.scm.lifecycle;

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.event.RecreateEventBusEvent;
import sonia.scm.event.ScmEventBus;
import sonia.scm.event.ShutdownEventBusEvent;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Restart strategy implementation which destroy the injection context and re initialize it.
 */
public class InjectionContextRestartStrategy implements RestartStrategy {

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
