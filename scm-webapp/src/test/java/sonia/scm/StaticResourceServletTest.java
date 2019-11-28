package sonia.scm;

import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StaticResourceServletTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private ServletOutputStream stream;

  @Mock
  private HttpServletResponse response;

  @Mock
  private ServletContext context;

  @Test
  void shouldServeResource() throws IOException {
    doReturn("/scm").when(request).getContextPath();
    doReturn("/scm/resource.txt").when(request).getRequestURI();
    doReturn(context).when(request).getServletContext();
    URL resource = Resources.getResource("sonia/scm/lifecycle/resource.txt");
    doReturn(resource).when(context).getResource("/resource.txt");
    doReturn(stream).when(response).getOutputStream();

    StaticResourceServlet servlet = new StaticResourceServlet();
    servlet.doGet(request, response);

    verify(response).setStatus(HttpServletResponse.SC_OK);
  }

  @Test
  void shouldReturnNotFound() {
    doReturn("/scm").when(request).getContextPath();
    doReturn("/scm/resource.txt").when(request).getRequestURI();
    doReturn(context).when(request).getServletContext();

    StaticResourceServlet servlet = new StaticResourceServlet();
    servlet.doGet(request, response);

    verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
  }

}
