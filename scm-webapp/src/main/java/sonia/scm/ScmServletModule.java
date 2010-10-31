/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.multibindings.Multibinder;
import com.google.inject.servlet.ServletModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.api.rest.UriExtensionsConfig;
import sonia.scm.cache.CacheManager;
import sonia.scm.cache.CacheRepositoryManagerDecorator;
import sonia.scm.cache.EhCacheManager;
import sonia.scm.filter.SecurityFilter;
import sonia.scm.plugin.ScriptResourceServlet;
import sonia.scm.repository.BasicRepositoryManager;
import sonia.scm.repository.RepositoryHandler;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.security.EncryptionHandler;
import sonia.scm.security.MessageDigestEncryptionHandler;
import sonia.scm.util.DebugServlet;
import sonia.scm.util.Util;
import sonia.scm.web.plugin.SCMPlugin;
import sonia.scm.web.plugin.SCMPluginManager;
import sonia.scm.web.plugin.ScmWebPluginContext;
import sonia.scm.web.security.Authenticator;
import sonia.scm.web.security.BasicSecurityContext;
import sonia.scm.web.security.SecurityContext;
import sonia.scm.web.security.XmlAuthenticator;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import java.io.IOException;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public class ScmServletModule extends ServletModule
{

  /** Field description */
  public static final String PATTERN_DEBUG = "/debug.html";

  /** Field description */
  public static final String PATTERN_PAGE = "*.html";

  /** Field description */
  public static final String PATTERN_PLUGIN_SCRIPT = "/plugins/sonia.plugin.js";

  /** Field description */
  public static final String PATTERN_RESTAPI = "/api/rest/*";

  /** Field description */
  public static final String PATTERN_SCRIPT = "*.js";

  /** Field description */
  public static final String PATTERN_STYLESHEET = "*.css";

  /** Field description */
  public static final String REST_PACKAGE = "sonia.scm.api.rest";

  /** Field description */
  public static final String[] PATTERN_STATIC_RESOURCES = new String[] {
                                                            PATTERN_SCRIPT,
          PATTERN_STYLESHEET, "*.jpg", "*.gif", "*.png" };

  /** Field description */
  public static final String[] PATTERN_COMPRESSABLE = new String[] {
                                                        PATTERN_SCRIPT,
          PATTERN_STYLESHEET, "*.json", "*.xml", "*.txt" };

  /** Field description */
  private static Logger logger =
    LoggerFactory.getLogger(ScmServletModule.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param webPluginContext
   */
  ScmServletModule(ScmWebPluginContext webPluginContext)
  {
    this.webPluginContext = webPluginContext;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  protected void configureServlets()
  {
    SCMContextProvider context = SCMContext.getContext();

    bind(SCMContextProvider.class).toInstance(context);
    bind(EncryptionHandler.class).to(MessageDigestEncryptionHandler.class);
    bind(Authenticator.class).to(XmlAuthenticator.class);
    bind(SecurityContext.class).to(BasicSecurityContext.class);

    SCMPluginManager pluginManager = new SCMPluginManager();

    try
    {
      pluginManager.load();
    }
    catch (IOException ex)
    {
      logger.error(ex.getMessage(), ex);
    }

    loadPlugins(pluginManager);
    bind(CacheManager.class).to(EhCacheManager.class);
    bind(RepositoryManager.class).annotatedWith(Undecorated.class).to(
        BasicRepositoryManager.class);
    bind(RepositoryManager.class).to(CacheRepositoryManagerDecorator.class);
    bind(ScmWebPluginContext.class).toInstance(webPluginContext);

    /*
     * filter(PATTERN_PAGE,
     *      PATTERN_STATIC_RESOURCES).through(StaticResourceFilter.class);
     * filter(PATTERN_PAGE, PATTERN_COMPRESSABLE).through(GZipFilter.class);
     * filter(PATTERN_RESTAPI).through(SecurityFilter.class);
     */
    filter(PATTERN_RESTAPI, PATTERN_DEBUG).through(SecurityFilter.class);

    // debug servlet
    serve(PATTERN_DEBUG).with(DebugServlet.class);

    // plugin resources
    serve(PATTERN_PLUGIN_SCRIPT).with(ScriptResourceServlet.class);

    // jersey
    Map<String, String> params = new HashMap<String, String>();

    /*
     * params.put("com.sun.jersey.spi.container.ContainerRequestFilters",
     *          "com.sun.jersey.api.container.filter.LoggingFilter");
     * params.put("com.sun.jersey.spi.container.ContainerResponseFilters",
     *          "com.sun.jersey.api.container.filter.LoggingFilter");
     * params.put("com.sun.jersey.config.feature.Trace", "true");
     * params.put("com.sun.jersey.config.feature.TracePerRequest", "true");
     */
    params.put(ResourceConfig.FEATURE_REDIRECT, Boolean.TRUE.toString());
    params.put(ServletContainer.RESOURCE_CONFIG_CLASS,
               UriExtensionsConfig.class.getName());
    params.put(PackagesResourceConfig.PROPERTY_PACKAGES, REST_PACKAGE);
    serve(PATTERN_RESTAPI).with(GuiceContainer.class, params);
  }

  /**
   * Method description
   *
   *
   * @param repositoryHandlerBinder
   * @param handlerSet
   */
  private void bindRepositoryHandlers(
          Multibinder<RepositoryHandler> repositoryHandlerBinder,
          Set<Class<? extends RepositoryHandler>> handlerSet)
  {
    for (Class<? extends RepositoryHandler> handlerClass : handlerSet)
    {
      if (logger.isInfoEnabled())
      {
        logger.info("load RepositoryHandler {}", handlerClass.getName());
      }

      bind(handlerClass);
      repositoryHandlerBinder.addBinding().to(handlerClass);
    }
  }

  /**
   * Method description
   *
   *
   * @param pluginManager
   */
  private void loadPlugins(SCMPluginManager pluginManager)
  {
    Set<SCMPlugin> pluginSet = pluginManager.getPlugins();

    if (Util.isNotEmpty(pluginSet))
    {
      Multibinder<RepositoryHandler> repositoryHandlerBinder =
        Multibinder.newSetBinder(binder(), RepositoryHandler.class);
      Set<Class<? extends RepositoryHandler>> handlerSet =
        new LinkedHashSet<Class<? extends RepositoryHandler>>();

      for (SCMPlugin plugin : pluginSet)
      {
        Collection<Class<? extends RepositoryHandler>> handlers =
          plugin.getHandlers();

        if (Util.isNotEmpty(handlers))
        {
          handlerSet.addAll(handlers);
        }
      }

      bindRepositoryHandlers(repositoryHandlerBinder, handlerSet);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private ScmWebPluginContext webPluginContext;
}
