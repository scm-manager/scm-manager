package sonia.scm.web;

import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by omilke on 19.05.2017.
 */
public class GitPermissionFilterTest {

  @Test
  public void isLfsFileUpload() throws Exception {

    HttpServletRequest mockedRequest = getRequestWithMethodAndPathInfo("PUT",
                                                                       "/scm/git/git-lfs-demo.git/info/lfs/objects/8fcebeb5698230685f92028e560f8f1683ebc15ec82a620ffad5c12a3c19bdec");
    assertThat((GitPermissionFilter.isLfsFileUpload(mockedRequest)), is(true));

    mockedRequest = getRequestWithMethodAndPathInfo("GET",
                                                    "/scm/git/git-lfs-demo.git/info/lfs/objects/8fcebeb5698230685f92028e560f8f1683ebc15ec82a620ffad5c12a3c19bdec");
    assertThat((GitPermissionFilter.isLfsFileUpload(mockedRequest)), is(false));

    mockedRequest = getRequestWithMethodAndPathInfo("POST",
                                                    "/scm/git/git-lfs-demo.git/info/lfs/objects/8fcebeb5698230685f92028e560f8f1683ebc15ec82a620ffad5c12a3c19bdec");
    assertThat((GitPermissionFilter.isLfsFileUpload(mockedRequest)), is(false));

    mockedRequest = getRequestWithMethodAndPathInfo("POST",
                                                    "/scm/git/git-lfs-demo.git/info/lfs/objects/batch");
    assertThat((GitPermissionFilter.isLfsFileUpload(mockedRequest)), is(false));
  }

  private HttpServletRequest getRequestWithMethodAndPathInfo(String method, String pathInfo) {

    HttpServletRequest mock = mock(HttpServletRequest.class);

    when(mock.getMethod()).thenReturn(method);
    when(mock.getRequestURI()).thenReturn(pathInfo);
    when(mock.getContextPath()).thenReturn("/scm");

    return mock;
  }

}
