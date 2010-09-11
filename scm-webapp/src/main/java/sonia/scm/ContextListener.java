/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

import sonia.scm.api.rest.UriExtensionsConfig;
import sonia.scm.security.Authenticator;
import sonia.scm.security.DemoAuthenticator;

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
public class ContextListener extends GuiceServletContextListener
{

  /** Field description */
  public static final String REST_MAPPING = "/api/rest/*";

  /** Field description */
  public static final String REST_PACKAGE = "sonia.scm.api.rest";

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected Injector getInjector()
  {
    return Guice.createInjector(new ServletModule()
    {
      @Override
      protected void configureServlets()
      {
        bind(Authenticator.class).to(DemoAuthenticator.class);

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
        serve(REST_MAPPING).with(GuiceContainer.class, params);
      }
    });
  }
}
