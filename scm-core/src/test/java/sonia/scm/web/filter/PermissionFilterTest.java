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

package sonia.scm.web.filter;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.Rule;
import org.junit.Test;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.Repository;
import sonia.scm.repository.spi.ScmProviderHttpServlet;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SubjectAware(configuration = "classpath:sonia/scm/shiro.ini")
public class PermissionFilterTest {

  public static final Repository REPOSITORY = new Repository("1", "git", "space", "name");

  @Rule
  public final ShiroRule shiroRule = new ShiroRule();

  private final ScmProviderHttpServlet delegateServlet = mock(ScmProviderHttpServlet.class);

  private final PermissionFilter permissionFilter = new PermissionFilter(new ScmConfiguration(), delegateServlet) {
    @Override
    protected boolean isWriteRequest(HttpServletRequest request) {
      return writeRequest;
    }
  };

  private final HttpServletRequest request = mock(HttpServletRequest.class);
  private final HttpServletResponse response = mock(HttpServletResponse.class);

  private boolean writeRequest = false;

  @Test
  @SubjectAware(username = "reader", password = "secret")
  public void shouldPassForReaderOnReadRequest() throws IOException, ServletException {
    writeRequest = false;

    permissionFilter.service(request, response, REPOSITORY);

    verify(delegateServlet).service(request, response, REPOSITORY);
  }

  @Test
  @SubjectAware(username = "reader", password = "secret")
  public void shouldBlockForReaderOnWriteRequest() throws IOException, ServletException {
    writeRequest = true;

    permissionFilter.service(request, response, REPOSITORY);

    verify(response).sendError(eq(403));
    verify(delegateServlet, never()).service(request, response, REPOSITORY);
  }

  @Test
  @SubjectAware(username = "_anonymous", password = "secret")
  public void shouldBlockForAnonymousOnWriteRequestWithAuthenticationRequest() throws IOException, ServletException {
    writeRequest = true;

    permissionFilter.service(request, response, REPOSITORY);

    verify(response).sendError(eq(401), anyString());
    verify(delegateServlet, never()).service(request, response, REPOSITORY);
  }

  @Test
  @SubjectAware(username = "writer", password = "secret")
  public void shouldPassForWriterOnWriteRequest() throws IOException, ServletException {
    writeRequest = true;

    permissionFilter.service(request, response, REPOSITORY);

    verify(delegateServlet).service(request, response, REPOSITORY);
  }
}
