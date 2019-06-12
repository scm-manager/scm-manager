package sonia.scm.boot;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SingleViewTest {

  @Mock
  private ServletContext servletContext;

  @Mock
  private HttpServletRequest request;

  @Captor
  private ArgumentCaptor<Injector> captor;

  private GuiceFilter guiceFilter;

  @BeforeEach
  void setUpGuiceFilter() throws ServletException {
    guiceFilter = new GuiceFilter();
    FilterConfig config = mock(FilterConfig.class);
    doReturn(servletContext).when(config).getServletContext();
    guiceFilter.init(config);
  }

  @AfterEach
  void tearDownGuiceFilter() {
    guiceFilter.destroy();
  }

  @Test
  void shouldCreateViewControllerForError() {
    ServletContextListener listener = SingleView.error(new IOException("awesome io"));
    when(request.getContextPath()).thenReturn("/scm");

    ViewController instance = findViewController(listener);
    assertErrorViewController(instance, "awesome io");
  }

  @Test
  void shouldBindServlets() {
    ServletContextListener listener = SingleView.error(new IOException("awesome io"));
    Injector injector = findInjector(listener);

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

  private ViewController findViewController(ServletContextListener listener) {
    Injector injector = findInjector(listener);
    return injector.getInstance(ViewController.class);
  }

  private Injector findInjector(ServletContextListener listener) {
    listener.contextInitialized(new ServletContextEvent(servletContext));

    verify(servletContext).setAttribute(anyString(), captor.capture());

    return captor.getValue();
  }


}
