package sonia.scm.web;

import org.junit.Test;
import sonia.scm.repository.HgRepositoryHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sonia.scm.web.HgHookCallbackServlet.PARAM_REPOSITORYID;

public class HgHookCallbackServletTest {

  @Test
  public void shouldExtractCorrectRepositoryId() throws ServletException, IOException {
    HgRepositoryHandler handler = mock(HgRepositoryHandler.class);
    HgHookCallbackServlet servlet = new HgHookCallbackServlet(null, handler, null, null);
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getContextPath()).thenReturn("http://example.com/scm");
    when(request.getRequestURI()).thenReturn("http://example.com/scm/hook/hg/pretxnchangegroup");
    String path = "/tmp/hg/12345";
    when(request.getParameter(PARAM_REPOSITORYID)).thenReturn(path);

    servlet.doPost(request, response);

    verify(response, never()).sendError(anyInt());
  }
}
