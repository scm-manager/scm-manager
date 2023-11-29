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
    
package sonia.scm.lifecycle.view;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.servlet.ServletModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sonia.scm.Default;
import sonia.scm.SCMContext;
import sonia.scm.SCMContextProvider;
import sonia.scm.StaticResourceServlet;
import sonia.scm.lifecycle.modules.ModuleProvider;
import sonia.scm.lifecycle.modules.ServletContextModule;
import sonia.scm.template.MustacheTemplateEngine;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;

import java.util.Collection;

public final class SingleView {

  private SingleView() {
  }

  public static SingleViewModuleProvider error(Throwable throwable) {
    String error = Throwables.getStackTraceAsString(throwable);

    ViewController controller = new SimpleViewController("/templates/error.mustache", request -> {
      Object model = ImmutableMap.of(
        "contextPath", request.getContextPath(),
        "error", error
      );
      return new View(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, model);
    });
    return new SingleViewModuleProvider(controller);
  }

  public static SingleViewModuleProvider view(String template, int sc) {
    ViewController controller = new SimpleViewController(template, request -> {
      Object model = ImmutableMap.of(
        "contextPath", request.getContextPath()
      );
      return new View(sc, model);
    });
    return new SingleViewModuleProvider(controller);
  }

  private static class SingleViewModuleProvider implements ModuleProvider {

    private final ViewController controller;

    private SingleViewModuleProvider(ViewController controller) {
      this.controller = controller;
    }

    @Override
    public Collection<Module> createModules() {
      return ImmutableList.of(new ServletContextModule(), new SingleViewModule(controller));
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

      serve("/images/*", "/assets/*", "/favicon.ico").with(StaticResourceServlet.class);
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
