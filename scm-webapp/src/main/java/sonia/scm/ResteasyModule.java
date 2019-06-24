package sonia.scm;

import com.google.common.collect.ImmutableMap;
import com.google.inject.servlet.ServletModule;
import org.jboss.resteasy.plugins.server.servlet.ResteasyContextParameters;

import java.util.Map;

/**
 * Module to configure resteasy with guice.
 */
public class ResteasyModule extends ServletModule {
  @Override
  protected void configureServlets() {
    Map<String, String> initParams = ImmutableMap.of(ResteasyContextParameters.RESTEASY_SERVLET_MAPPING_PREFIX, "/api");
    serve("/api/*").with(ResteasyAllInOneServletDispatcher.class, initParams);
  }
}
