package sonia.scm.security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Created by masuewer on 04.07.18.
 */
@RunWith(MockitoJUnitRunner.class)
public class SecurityRequestsTest {

  @Mock
  private HttpServletRequest request;

  @Test
  public void testIsAuthenticationRequestWithContextPath() {
    when(request.getRequestURI()).thenReturn("/scm/api/rest/auth/access_token");
    when(request.getContextPath()).thenReturn("/scm");

    assertTrue(SecurityRequests.isAuthenticationRequest(request));
  }

  @Test
  public void testIsAuthenticationRequest() throws Exception {
    assertTrue(SecurityRequests.isAuthenticationRequest("/api/rest/auth/access_token"));
    assertTrue(SecurityRequests.isAuthenticationRequest("/api/rest/v2/auth/access_token"));
    assertFalse(SecurityRequests.isAuthenticationRequest("/api/rest/repositories"));
    assertFalse(SecurityRequests.isAuthenticationRequest("/api/rest/v2/repositories"));
  }
}
