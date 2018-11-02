package sonia.scm;

import com.google.common.collect.ImmutableMap;
import com.google.inject.servlet.ServletModule;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyContextParameters;

import javax.inject.Singleton;
import java.util.Map;

public class ResteasyModule extends ServletModule {

  @Override
  protected void configureServlets() {
    bind(HttpServletDispatcher.class).in(Singleton.class);

    Map<String, String> initParams = ImmutableMap.of(ResteasyContextParameters.RESTEASY_SERVLET_MAPPING_PREFIX, "/api");
    serve("/api/*").with(HttpServletDispatcher.class, initParams);
  }
}
