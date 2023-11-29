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
    
package sonia.scm;

import com.google.common.io.Resources;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
