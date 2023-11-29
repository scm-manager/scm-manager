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
    
package sonia.scm.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.config.ScmConfiguration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
    issuer.authenticate(request, response, accessToken);

    verify(response).addCookie(cookieArgumentCaptor.capture());
    return cookieArgumentCaptor.getValue();
  }


  private void assertContextPath(String contextPath, String expected) {
    when(request.getContextPath()).thenReturn(contextPath);
    assertEquals(expected, issuer.contextPath(request));
  }
}
