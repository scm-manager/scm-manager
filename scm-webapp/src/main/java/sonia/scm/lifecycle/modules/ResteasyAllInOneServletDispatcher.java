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

package sonia.scm.lifecycle.modules;

import com.google.inject.Injector;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.ws.rs.ext.RuntimeDelegate;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.plugins.server.servlet.ListenerBootstrap;
import org.jboss.resteasy.spi.Registry;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.spi.statistics.StatisticsController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.lifecycle.modules.resteasyguice.ModuleProcessor;

/**
 * Resteasy initialization and dispatching. This servlet combines the initialization of
 * {@link HttpServletDispatcher}. The combination is required to fix the initialization order.
 */
@Singleton
public class ResteasyAllInOneServletDispatcher extends HttpServletDispatcher {

  private static final Logger LOG = LoggerFactory.getLogger(ResteasyAllInOneServletDispatcher.class);

  private final Injector injector;

  @Inject
  public ResteasyAllInOneServletDispatcher(Injector injector) {
    this.injector = injector;
  }

  @Override
  public void init(ServletConfig servletConfig) throws ServletException {
    LOG.info("init resteasy");

    ServletContext servletContext = servletConfig.getServletContext();
    ResteasyDeployment deployment = createDeployment(servletContext);

    ModuleProcessor processor = createModuleProcessor(deployment);
    processor.processInjector(injector);

    super.init(servletConfig);
  }

  private ResteasyDeployment createDeployment(ServletContext servletContext) {
    ListenerBootstrap config = new ListenerBootstrap(servletContext);
    ResteasyDeployment deployment = config.createDeployment();
    deployment.start();

    servletContext.setAttribute(ResteasyDeployment.class.getName(), deployment);
    return deployment;
  }

  private ModuleProcessor createModuleProcessor(ResteasyDeployment deployment) {
    Registry registry = deployment.getRegistry();
    ResteasyProviderFactory providerFactory = deployment.getProviderFactory();
    return new ModuleProcessor(registry, providerFactory);
  }

  @Override
  public void destroy() {
    LOG.info("destroy resteasy");
    ResteasyDeployment deployment = getDeploymentFromServletContext();

    super.destroy();
    deployment.stop();

    // clear ResourceLocatorInvoker leaks
    StatisticsController statisticsController = ResteasyProviderFactory.getInstance().getStatisticsController();
    if (statisticsController != null) {
      statisticsController.reset();
    }

    // ensure everything gets cleared, to avoid classloader leaks
    ResteasyProviderFactory.clearInstanceIfEqual(ResteasyProviderFactory.getInstance());
    RuntimeDelegate.setInstance(null);

    removeDeploymentFromServletContext();
  }

  private void removeDeploymentFromServletContext() {
    getServletContext().removeAttribute(ResteasyDeployment.class.getName());
  }

  private ResteasyDeployment getDeploymentFromServletContext() {
    return (ResteasyDeployment) getServletContext().getAttribute(ResteasyDeployment.class.getName());
  }
}
