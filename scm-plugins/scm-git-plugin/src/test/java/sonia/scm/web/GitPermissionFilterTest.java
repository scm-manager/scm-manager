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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.spi.ScmProviderHttpServlet;
import sonia.scm.util.HttpUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GitPermissionFilter}.
 *
 * Created by omilke on 19.05.2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class GitPermissionFilterTest {

  private final GitPermissionFilter permissionFilter = new GitPermissionFilter(new ScmConfiguration(), mock(ScmProviderHttpServlet.class));

  @Mock
  private HttpServletResponse response;

  @Test
  public void testIsWriteRequest() {
    HttpServletRequest request = mockRequestWithMethodAndRequestURI("POST", "/scm/git/fanzy-project/git-receive-pack");
    assertThat(permissionFilter.isWriteRequest(request), is(true));

    request = mockRequestWithMethodAndRequestURI("GET", "/scm/git/fanzy-project/info/refs?service=git-receive-pack");
    assertThat(permissionFilter.isWriteRequest(request), is(true));

    request = mockRequestWithMethodAndRequestURI("GET", "/scm/git/fanzy-project/info/refs?service=some-other-service");
    assertThat(permissionFilter.isWriteRequest(request), is(false));

    request = mockRequestWithMethodAndRequestURI(
      "PUT",
      "/scm/git/git-lfs-demo.git/info/lfs/objects/8fcebeb5698230685f92028e560f8f1683ebc15ec82a620ffad5c12a3c19bdec"
    );
    assertThat(permissionFilter.isWriteRequest(request), is(true));

    request = mockRequestWithMethodAndRequestURI(
      "GET",
      "/scm/git/git-lfs-demo.git/info/lfs/objects/8fcebeb5698230685f92028e560f8f1683ebc15ec82a620ffad5c12a3c19bdec"
    );
    assertThat(permissionFilter.isWriteRequest(request), is(false));

    request = mockRequestWithMethodAndRequestURI("POST", "/scm/git/git-lfs-demo.git/info/lfs/objects/batch");
    assertThat(permissionFilter.isWriteRequest(request), is(false));
  }

  private HttpServletRequest mockRequestWithMethodAndRequestURI(String method, String requestURI) {
    HttpServletRequest mock = mock(HttpServletRequest.class);

    when(mock.getMethod()).thenReturn(method);
    when(mock.getRequestURI()).thenReturn(requestURI);

    return mock;
  }

  @Test
  public void testSendNotEnoughPrivilegesErrorAsBrowser() throws IOException {
    HttpServletRequest request = mockGitReceivePackServiceRequest();

    permissionFilter.sendNotEnoughPrivilegesError(request, response);

    verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
  }

  @Test
  public void testSendNotEnoughPrivilegesErrorAsGitClient() throws IOException {
    verifySendNotEnoughPrivilegesErrorAsGitClient("git/2.9.3");
  }

  @Test
  public void testSendNotEnoughPrivilegesErrorAsJGitClient() throws IOException {
    verifySendNotEnoughPrivilegesErrorAsGitClient("JGit/4.2");
  }

  private void verifySendNotEnoughPrivilegesErrorAsGitClient(String userAgent) throws IOException {
    HttpServletRequest request = mockGitReceivePackServiceRequest();
    when(request.getHeader(HttpUtil.HEADER_USERAGENT)).thenReturn(userAgent);

    CapturingServletOutputStream stream = new CapturingServletOutputStream();
    when(response.getOutputStream()).thenReturn(stream);

    permissionFilter.sendNotEnoughPrivilegesError(request, response);

    verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
    assertThat(stream.toString(), containsString("privileges"));
  }

  private HttpServletRequest mockGitReceivePackServiceRequest() {
    HttpServletRequest request = mockRequestWithMethodAndRequestURI("GET", "/git/info/refs");
    when(request.getParameter("service")).thenReturn("git-receive-pack");
    return request;
  }

}
