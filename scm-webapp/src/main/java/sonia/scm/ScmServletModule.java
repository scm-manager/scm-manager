/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.servlet.ServletModule;

import sonia.scm.api.rest.UriExtensionsConfig;
import sonia.scm.plugin.ScriptResourceServlet;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.web.ScmWebPluginContext;
import sonia.scm.web.security.Authenticator;
import sonia.scm.web.security.DemoAuthenticator;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 */
public class ScmServletModule extends ServletModule
{

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
    bind(Authenticator.class).to(DemoAuthenticator.class);
    bind(RepositoryManager.class).toInstance(context.getRepositoryManager());
    bind(ScmWebPluginContext.class).toInstance(webPluginContext);

    /*
     * filter(PATTERN_PAGE,
     *      PATTERN_STATIC_RESOURCES).through(StaticResourceFilter.class);
     * filter(PATTERN_PAGE, PATTERN_COMPRESSABLE).through(GZipFilter.class);
     * filter(PATTERN_RESTAPI).through(SecurityFilter.class);
     */

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

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private ScmWebPluginContext webPluginContext;
}
