package sonia.scm.boot;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import sonia.scm.Default;
import sonia.scm.SCMContext;
import sonia.scm.SCMContextProvider;
import sonia.scm.template.MustacheTemplateEngine;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

final class SingleView {

  private SingleView() {
  }

  static ServletContextListener error(Throwable throwable) {
    String error = Throwables.getStackTraceAsString(throwable);

    ViewController controller = new SimpleViewController("/templates/error.mustache", request -> {
      Object model = ImmutableMap.of(
        "contextPath", request.getContextPath(),
        "error", error
      );
      return new View(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, model);
    });
    return new SingleViewContextListener(controller);
  }

  private static class SingleViewContextListener extends GuiceServletContextListener {

    private final ViewController controller;

    private SingleViewContextListener(ViewController controller) {
      this.controller = controller;
    }

    @Override
    protected Injector getInjector() {
      return Guice.createInjector(new SingleViewModule(controller));
    }
  }

  private static class SingleViewModule extends ServletModule {

    private final ViewController viewController;

    private SingleViewModule(ViewController viewController) {
      this.viewController = viewController;
    }

    @Override
    protected void configureServlets() {
      SCMContextProvider context = SCMContext.getContext();

      bind(SCMContextProvider.class).toInstance(context);
      bind(ViewController.class).toInstance(viewController);

      Multibinder<TemplateEngine> engineBinder =
        Multibinder.newSetBinder(binder(), TemplateEngine.class);

      engineBinder.addBinding().to(MustacheTemplateEngine.class);
      bind(TemplateEngine.class).annotatedWith(Default.class).to(
        MustacheTemplateEngine.class);
      bind(TemplateEngineFactory.class);

      bind(ServletContext.class).annotatedWith(Default.class).toInstance(getServletContext());

      serve("/images/*", "/styles/*", "/favicon.ico").with(StaticResourceServlet.class);
      serve("/*").with(SingleViewServlet.class);
    }
  }

  private static class SimpleViewController implements ViewController {

    private final String template;
    private final SimpleViewFactory viewFactory;

    private SimpleViewController(String template, SimpleViewFactory viewFactory) {
      this.template = template;
      this.viewFactory = viewFactory;
    }

    @Override
    public String getTemplate() {
      return template;
    }

    @Override
    public View createView(HttpServletRequest request) {
      return viewFactory.create(request);
    }
  }

  @FunctionalInterface
  interface SimpleViewFactory {
    View create(HttpServletRequest request);
  }
}
