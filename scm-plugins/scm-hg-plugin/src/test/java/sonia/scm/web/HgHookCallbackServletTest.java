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

package sonia.scm.web;

import com.google.inject.util.Providers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.HgContext;
import sonia.scm.repository.HgHookManager;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.spi.HookEventFacade;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static sonia.scm.web.HgHookCallbackServlet.*;

@ExtendWith(MockitoExtension.class)
class HgHookCallbackServletTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private HgRepositoryHandler repositoryHandler;

  @Mock
  private HgHookManager hookManager;

  @Mock
  private HgContext context;

  private HgHookCallbackServlet servlet;

  @BeforeEach
  void setUpObjectUnderTest() {
    servlet = new HgHookCallbackServlet(null, repositoryHandler, hookManager, Providers.of(context));
  }

  @Test
  void shouldExtractCorrectRepositoryId() throws ServletException, IOException {
    when(request.getContextPath()).thenReturn("http://example.com/scm");
    when(request.getRequestURI()).thenReturn("http://example.com/scm/hook/hg/pretxnchangegroup");
    String path = "/tmp/hg/12345";
    when(request.getParameter(PARAM_REPOSITORYID)).thenReturn(path);

    servlet.doPost(request, response);

    verify(response, never()).sendError(anyInt());
  }

  @Test
  void shouldReturnWithSignedChallenge() throws IOException, ServletException {
    when(request.getMethod()).thenReturn("GET");
    when(request.getParameter(PARAM_PING)).thenReturn("true");
    when(request.getParameter(PARAM_CHALLENGE)).thenReturn("awesome-challenge");
    when(hookManager.sign("awesome-challenge")).thenReturn("challenge-awesome");

    StringWriter writer = new StringWriter();
    when(response.getWriter()).thenReturn(new PrintWriter(writer, true));

    servlet.service(request, response);

    verify(response).setStatus(200);
    assertThat(writer).hasToString("challenge-awesome");
  }

  @Test
  void shouldReturnStatus400WithoutChallenge() throws IOException, ServletException {
    when(request.getMethod()).thenReturn("GET");
    when(request.getParameter(PARAM_PING)).thenReturn("true");

    servlet.service(request, response);

    verify(response).setStatus(400);
  }

  @Test
  void shouldReturnStatus404OnGetWithoutPingParameter() throws IOException, ServletException {
    when(request.getMethod()).thenReturn("GET");

    servlet.service(request, response);

    verify(response).setStatus(404);
  }

  @Test
  void shouldReturnStatus404OnGetWithNonTruePingParameter() throws IOException, ServletException {
    when(request.getMethod()).thenReturn("GET");
    when(request.getParameter(PARAM_PING)).thenReturn("other");

    servlet.service(request, response);

    verify(response).setStatus(404);
  }
}
