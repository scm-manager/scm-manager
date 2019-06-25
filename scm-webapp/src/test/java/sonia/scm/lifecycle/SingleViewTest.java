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
import sonia.scm.StaticResourceServlet;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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
