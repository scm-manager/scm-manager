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

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.security.TokenExpiredException;
import sonia.scm.util.HttpUtil;
import sonia.scm.web.UserAgent;
import sonia.scm.web.UserAgentParser;
import sonia.scm.web.WebTokenGenerator;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HttpProtocolServletAuthenticationFilterBaseTest {

  private ScmConfiguration configuration;

  private Set<WebTokenGenerator> tokenGenerators = Collections.emptySet();

  @Mock
  private UserAgentParser userAgentParser;

  private HttpProtocolServletAuthenticationFilterBase authenticationFilter;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain filterChain;

  private UserAgent nonBrowser = UserAgent.other("i'm not a browser").build();
  private UserAgent browser = UserAgent.browser("i am a browser").build();

  @BeforeEach
  void setUpObjectUnderTest() {
    configuration = new ScmConfiguration();
    authenticationFilter = new HttpProtocolServletAuthenticationFilterBase(configuration, tokenGenerators, userAgentParser);
  }

  @Test
  void shouldSendUnauthorized() throws IOException, ServletException {
    when(userAgentParser.parse(request)).thenReturn(nonBrowser);

    authenticationFilter.handleUnauthorized(request, response, filterChain);

    verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, HttpUtil.STATUS_UNAUTHORIZED_MESSAGE);
  }

  @Test
  void shouldSendConfiguredRealmDescription() throws IOException, ServletException {
    configuration.setRealmDescription("Hitchhikers finest");
    when(userAgentParser.parse(request)).thenReturn(nonBrowser);

    authenticationFilter.handleUnauthorized(request, response, filterChain);

    verify(response).setHeader(HttpUtil.HEADER_WWW_AUTHENTICATE, "Basic realm=\"Hitchhikers finest\"");
  }

  @Test
  void shouldCallFilterChain() throws IOException, ServletException {
    when(userAgentParser.parse(request)).thenReturn(browser);

    authenticationFilter.handleUnauthorized(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  void shouldIgnoreTokenExpiredExceptionForBrowserCall() throws IOException, ServletException {
    when(userAgentParser.parse(request)).thenReturn(browser);

    authenticationFilter.handleTokenExpiredException(request, response, filterChain, new TokenExpiredException("Nothing ever expired so much"));

    verify(filterChain).doFilter(request, response);
  }

  @Test
  void shouldRethrowTokenExpiredExceptionForApiCall() {
    when(userAgentParser.parse(request)).thenReturn(nonBrowser);

    final TokenExpiredException tokenExpiredException = new TokenExpiredException("Nothing ever expired so much");

    assertThrows(TokenExpiredException.class,
      () -> authenticationFilter.handleTokenExpiredException(request, response, filterChain, tokenExpiredException));
  }

}
