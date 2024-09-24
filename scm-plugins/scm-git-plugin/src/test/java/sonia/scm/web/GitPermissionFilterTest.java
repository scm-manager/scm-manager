/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.spi.ScmProviderHttpServlet;
import sonia.scm.util.HttpUtil;

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
