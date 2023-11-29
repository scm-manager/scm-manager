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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.StaticResourceServlet;
import sonia.scm.lifecycle.modules.ModuleProvider;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SingleViewTest {

  @Mock
  private HttpServletRequest request;

  private GuiceFilter guiceFilter;

  @BeforeEach
  void setUpGuiceFilter() throws ServletException {
    guiceFilter = new GuiceFilter();

    ServletContext servletContext = mock(ServletContext.class);
    FilterConfig config = mock(FilterConfig.class);
    doReturn(servletContext).when(config).getServletContext();

    guiceFilter.init(config);
  }

  @AfterEach
  void tearDownGuiceFilter() {
    guiceFilter.destroy();
  }

  @Test
  void shouldCreateViewControllerForView() {
    ModuleProvider moduleProvider = SingleView.view("/my-template", 409);
    when(request.getContextPath()).thenReturn("/scm");

    ViewController instance = findViewController(moduleProvider);
    assertThat(instance.getTemplate()).isEqualTo("/my-template");

    View view = instance.createView(request);
    assertThat(view.getStatusCode()).isEqualTo(409);
  }

  @Test
  void shouldCreateViewControllerForError() {
    ModuleProvider moduleProvider = SingleView.error(new IOException("awesome io"));
    when(request.getContextPath()).thenReturn("/scm");

    ViewController instance = findViewController(moduleProvider);
    assertErrorViewController(instance, "awesome io");
  }

  @Test
  void shouldBindServlets() {
    ModuleProvider moduleProvider = SingleView.error(new IOException("awesome io"));
    Injector injector = Guice.createInjector(moduleProvider.createModules());

    assertThat(injector.getInstance(StaticResourceServlet.class)).isNotNull();
    assertThat(injector.getInstance(SingleViewServlet.class)).isNotNull();
  }

  @SuppressWarnings("unchecked")
  private void assertErrorViewController(ViewController instance, String contains) {
    assertThat(instance.getTemplate()).isEqualTo("/templates/error.mustache");

    View view = instance.createView(request);
    assertThat(view.getStatusCode()).isEqualTo(500);
    assertThat(view.getModel()).isInstanceOfSatisfying(Map.class, map -> {
        assertThat(map).containsEntry("contextPath", "/scm");
        String error = (String) map.get("error");
        assertThat(error).contains(contains);
      }
    );
  }

  private ViewController findViewController(ModuleProvider moduleProvider) {
    Injector injector = Guice.createInjector(moduleProvider.createModules());
    return injector.getInstance(ViewController.class);
  }

}
