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
