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
