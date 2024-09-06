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
