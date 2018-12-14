package sonia.scm.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.config.ScmConfiguration;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultAccessTokenCookieIssuerTest {

  private ScmConfiguration configuration;

  private DefaultAccessTokenCookieIssuer issuer;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private AccessToken accessToken;

  @Captor
  private ArgumentCaptor<Cookie> cookieArgumentCaptor;

  @Before
  public void setUp() {
    configuration = new ScmConfiguration();
    issuer = new DefaultAccessTokenCookieIssuer(configuration);
  }

  @Test
  public void testContextPath() {
    assertContextPath("/scm", "/scm");
    assertContextPath("/", "/");
    assertContextPath("", "/");
    assertContextPath(null, "/");
  }

  @Test
  public void httpOnlyShouldBeEnabledIfXsrfProtectionIsDisabled() {
    configuration.setEnabledXsrfProtection(false);

    Cookie cookie = authenticate();

    assertTrue(cookie.isHttpOnly());
  }

  @Test
  public void httpOnlyShouldBeDisabled() {
    Cookie cookie = authenticate();

    assertFalse(cookie.isHttpOnly());
  }

  @Test
  public void secureShouldBeSetIfTheRequestIsSecure() {
    when(request.isSecure()).thenReturn(true);

    Cookie cookie = authenticate();

    assertTrue(cookie.getSecure());
  }

  @Test
  public void secureShouldBeDisabledIfTheRequestIsNotSecure() {
    when(request.isSecure()).thenReturn(false);

    Cookie cookie = authenticate();

    assertFalse(cookie.getSecure());
  }

  @Test
  public void testInvalidate() {
    issuer.invalidate(request, response);

    verify(response).addCookie(cookieArgumentCaptor.capture());
    Cookie cookie = cookieArgumentCaptor.getValue();

    assertEquals(0, cookie.getMaxAge());
  }

  private Cookie authenticate() {
    when(accessToken.getExpiration()).thenReturn(new Date());

    issuer.authenticate(request, response, accessToken);

    verify(response).addCookie(cookieArgumentCaptor.capture());
    return cookieArgumentCaptor.getValue();
  }


  private void assertContextPath(String contextPath, String expected) {
    when(request.getContextPath()).thenReturn(contextPath);
    assertEquals(expected, issuer.contextPath(request));
  }
}
