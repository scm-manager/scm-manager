/**
 * Copyright (c) 2014, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.boot;

//~--- non-JDK imports --------------------------------------------------------

import com.github.legman.Subscribe;
import com.google.inject.servlet.GuiceFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.event.ScmEventBus;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class BootstrapContextFilter extends GuiceFilter {

  /**
   * the logger for BootstrapContextFilter
   */
  private static final Logger LOG = LoggerFactory.getLogger(BootstrapContextFilter.class);

  private final BootstrapContextListener listener = new BootstrapContextListener();

  /** Field description */
  private FilterConfig filterConfig;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    this.filterConfig = filterConfig;

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
    ServletContextCleaner.cleanup(filterConfig.getServletContext());
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
      RestartStrategy restartStrategy = RestartStrategy.get();
      restartStrategy.restart(new GuiceInjectionContext());
    }
  }

  private class GuiceInjectionContext implements RestartStrategy.InjectionContext {

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
