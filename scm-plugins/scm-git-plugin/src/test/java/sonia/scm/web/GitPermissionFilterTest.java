package sonia.scm.web;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.RepositoryProvider;
import sonia.scm.util.HttpUtil;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link GitPermissionFilter}.
 * 
 * Created by omilke on 19.05.2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class GitPermissionFilterTest {

  @Mock
  private RepositoryProvider repositoryProvider;
  
  private final GitPermissionFilter permissionFilter = new GitPermissionFilter(
    new ScmConfiguration(), repositoryProvider
  );
  
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
    when(mock.getContextPath()).thenReturn("/scm");

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
    
    verify(response).setStatus(HttpServletResponse.SC_OK);
    assertThat(stream.toString(), containsString("privileges"));    
  }
  
  private HttpServletRequest mockGitReceivePackServiceRequest() {
    HttpServletRequest request = mockRequestWithMethodAndRequestURI("GET", "/git/info/refs");
    when(request.getParameter("service")).thenReturn("git-receive-pack");
    return request;
  }
  
  private static class CapturingServletOutputStream extends ServletOutputStream {

    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    
    @Override
    public void write(int b) throws IOException {
      baos.write(b);
    }

    @Override
    public void close() throws IOException {
      baos.close();
    }
    
    @Override
    public String toString() {
      return baos.toString();
    }
  }
  
}
