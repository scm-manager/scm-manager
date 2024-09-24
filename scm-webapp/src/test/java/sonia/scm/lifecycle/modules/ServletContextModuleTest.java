/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
