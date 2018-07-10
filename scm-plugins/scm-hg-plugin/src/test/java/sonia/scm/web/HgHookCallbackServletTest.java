package sonia.scm.web;

import org.junit.Test;
import sonia.scm.repository.HgConfig;
import sonia.scm.repository.HgRepositoryHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sonia.scm.web.HgHookCallbackServlet.PARAM_REPOSITORYPATH;

public class HgHookCallbackServletTest {

  @Test
  public void shouldExtractCorrectRepositoryId() throws ServletException, IOException {
    HgRepositoryHandler handler = mock(HgRepositoryHandler.class);
    HgHookCallbackServlet servlet = new HgHookCallbackServlet(null, handler, null, null);
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    HgConfig config = mock(HgConfig.class);

    when(request.getContextPath()).thenReturn("http://example.com/scm");
    when(request.getRequestURI()).thenReturn("http://example.com/scm/hook/hg/pretxnchangegroup");
    when(request.getParameter(PARAM_REPOSITORYPATH)).thenReturn("/tmp/hg/12345");

    when(handler.getConfig()).thenReturn(config);
    when(config.getRepositoryDirectory()).thenReturn(new File("/tmp/hg"));

    servlet.doPost(request, response);

    verify(response, never()).sendError(anyInt());
  }
}
