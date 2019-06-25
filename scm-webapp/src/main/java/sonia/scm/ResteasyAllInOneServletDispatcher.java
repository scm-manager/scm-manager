package sonia.scm;

import com.google.inject.Injector;
import org.jboss.resteasy.plugins.guice.ModuleProcessor;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.plugins.server.servlet.ListenerBootstrap;
import org.jboss.resteasy.spi.Registry;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.ws.rs.ext.RuntimeDelegate;

/**
 * Resteasy initialization and dispatching. This servlet combines the initialization of
 * {@link org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener} and the dispatching of
 * {@link HttpServletDispatcher}. The combination is required to fix the initialization order.
 */
@Singleton
public class ResteasyAllInOneServletDispatcher extends HttpServletDispatcher {

  private static final Logger LOG = LoggerFactory.getLogger(ResteasyAllInOneServletDispatcher.class);

  private final Injector injector;
  private ResteasyDeployment deployment;

  @Inject
  public ResteasyAllInOneServletDispatcher(Injector injector) {
    this.injector = injector;
  }

  @Override
  public void init(ServletConfig servletConfig) throws ServletException {
    LOG.info("init resteasy");

    ServletContext servletContext = servletConfig.getServletContext();
    createDeployment(servletContext);

    ModuleProcessor processor = createModuleProcessor();
    processor.processInjector(injector);

    super.init(servletConfig);
  }

  private void createDeployment(ServletContext servletContext) {
    ListenerBootstrap config = new ListenerBootstrap(servletContext);
    deployment = config.createDeployment();
    deployment.start();

    servletContext.setAttribute(ResteasyDeployment.class.getName(), deployment);
  }

  private ModuleProcessor createModuleProcessor() {
    Registry registry = deployment.getRegistry();
    ResteasyProviderFactory providerFactory = deployment.getProviderFactory();
    return new ModuleProcessor(registry, providerFactory);
  }

  @Override
  public void destroy() {
    LOG.info("destroy resteasy");
    super.destroy();
    deployment.stop();

    // ensure everything gets cleared, to avoid classloader leaks
    ResteasyProviderFactory.clearInstanceIfEqual(ResteasyProviderFactory.getInstance());
    ResteasyProviderFactory.clearContextData();
    RuntimeDelegate.setInstance(null);
  }
}
