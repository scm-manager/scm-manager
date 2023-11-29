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
    
package sonia.scm.lifecycle.modules;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import jakarta.inject.Inject;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.Default;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServletContextModuleTest {

  @Mock
  private ServletContext servletContext;

  private GuiceFilter guiceFilter;

  @BeforeEach
  void setUpEnvironment() throws ServletException {
    guiceFilter = new GuiceFilter();
    FilterConfig filterConfig = mock(FilterConfig.class);
    when(filterConfig.getServletContext()).thenReturn(servletContext);

    guiceFilter.init(filterConfig);
  }

  @AfterEach
  void tearDownEnvironment() {
    guiceFilter.destroy();
  }

  @Test
  void shouldBeAbleToInjectServletContext() {
    Injector injector = Guice.createInjector(new ServletContextModule());
    WebComponent instance = injector.getInstance(WebComponent.class);
    assertThat(instance.context).isSameAs(servletContext);
  }


  public static class WebComponent {

    private ServletContext context;

    @Inject
    public WebComponent(@Default ServletContext context) {
      this.context = context;
    }

  }

}
