package sonia.scm.security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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
    when(request.getRequestURI()).thenReturn("/scm/api/auth/access_token");
    when(request.getContextPath()).thenReturn("/scm");

    assertTrue(SecurityRequests.isAuthenticationRequest(request));
  }

  @Test
  public void testIsAuthenticationRequest() throws Exception {
    assertTrue(SecurityRequests.isAuthenticationRequest("/api/auth/access_token"));
    assertTrue(SecurityRequests.isAuthenticationRequest("/api/v2/auth/access_token"));
    assertFalse(SecurityRequests.isAuthenticationRequest("/api/repositories"));
    assertFalse(SecurityRequests.isAuthenticationRequest("/api/v2/repositories"));
  }
}
