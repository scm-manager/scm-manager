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


import com.github.legman.Subscribe;
import com.google.inject.servlet.GuiceFilter;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.event.ScmEventBus;

import java.util.Optional;


public class BootstrapContextFilter extends GuiceFilter {

 
  private static final Logger LOG = LoggerFactory.getLogger(BootstrapContextFilter.class);

  private final BootstrapContextListener listener = new BootstrapContextListener();

  private ClassLoader webAppClassLoader;

  private FilterConfig filterConfig;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    this.filterConfig = filterConfig;
    // store webapp classloader for delayed restarts
    webAppClassLoader = Thread.currentThread().getContextClassLoader();
    initializeContext();
  }

  private void initializeContext() throws ServletException {
    super.init(filterConfig);

    LOG.info("register for restart events");
    ScmEventBus.getInstance().register(this);

    listener.contextInitialized(new ServletContextEvent(filterConfig.getServletContext()));
  }

  @Override
  public void destroy() {
    super.destroy();

    listener.contextDestroyed(new ServletContextEvent(filterConfig.getServletContext()));
  }

  /**
   * Restart SCM-Manager.
   *
   * @param event restart event
   */
  @Subscribe
  public void handleRestartEvent(RestartEvent event) {
    LOG.warn("received restart event from {} with reason: {}",
      event.getCause(), event.getReason());

    if (filterConfig == null) {
      LOG.error("filter config is null, scm-manager is not initialized");
    } else {
      Optional<RestartStrategy> restartStrategy = RestartStrategy.get(webAppClassLoader);
      if (restartStrategy.isPresent()) {
        restartStrategy.get().restart(new GuiceInjectionContext());
      } else {
        LOG.warn("restarting is not supported by the underlying platform");
      }
    }
  }

  private class GuiceInjectionContext implements RestartStrategy.InternalInjectionContext {

    @Override
    public void initialize() {
      try {
        BootstrapContextFilter.this.initializeContext();
      } catch (ServletException e) {
        throw new IllegalStateException("failed to initialize guice", e);
      }
    }

    @Override
    public void destroy() {
      BootstrapContextFilter.this.destroy();
    }
  }

}
