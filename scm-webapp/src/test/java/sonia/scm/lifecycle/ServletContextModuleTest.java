package sonia.scm.lifecycle;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.Default;

import javax.inject.Inject;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

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
