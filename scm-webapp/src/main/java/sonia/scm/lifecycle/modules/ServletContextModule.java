package sonia.scm.lifecycle.modules;

import com.google.inject.servlet.ServletModule;
import sonia.scm.Default;

import javax.servlet.ServletContext;

public class ServletContextModule extends ServletModule {

  @Override
  protected void configureServlets() {
    bind(ServletContext.class).annotatedWith(Default.class).toInstance(getServletContext());
  }
}
