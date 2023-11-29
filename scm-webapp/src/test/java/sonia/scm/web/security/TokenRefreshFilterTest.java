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

package sonia.scm.web.security;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.AuthenticationToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.AccessTokenCookieIssuer;
import sonia.scm.security.AccessTokenResolver;
import sonia.scm.security.BearerToken;
import sonia.scm.security.JwtAccessToken;
import sonia.scm.security.JwtAccessTokenRefresher;
import sonia.scm.web.WebTokenGenerator;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

import static java.util.Collections.singleton;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static sonia.scm.security.BearerToken.valueOf;

@ExtendWith({MockitoExtension.class})
class TokenRefreshFilterTest {

  @Mock
  private Set<WebTokenGenerator> tokenGenerators;
  @Mock
  private WebTokenGenerator tokenGenerator;
  @Mock
  private JwtAccessTokenRefresher refresher;
  @Mock
  private AccessTokenResolver resolver;
  @Mock
  private AccessTokenCookieIssuer issuer;

  private TokenRefreshFilter filter;

  @Mock
  private HttpServletRequest request;
  @Mock
  private HttpServletResponse response;
  @Mock
  private FilterChain filterChain;

  private MeterRegistry meterRegistry;

  @BeforeEach
  void init() {
    when(tokenGenerators.iterator()).thenReturn(singleton(tokenGenerator).iterator());
    meterRegistry = new SimpleMeterRegistry();
    filter = new TokenRefreshFilter(tokenGenerators, refresher, resolver, issuer, meterRegistry);
  }

  @Test
  void shouldContinueChain() throws IOException, ServletException {
    filter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(issuer, never()).authenticate(any(), any(), any());
  }

  @Test
  void shouldNotRefreshNonBearerToken() throws IOException, ServletException {
    AuthenticationToken token = mock(AuthenticationToken.class);
    when(tokenGenerator.createToken(request)).thenReturn(token);

    filter.doFilter(request, response, filterChain);

    verify(issuer, never()).authenticate(any(), any(), any());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void shouldNotRefreshNonJwtToken() throws IOException, ServletException {
    BearerToken token = createValidToken();
    JwtAccessToken jwtToken = mock(JwtAccessToken.class);
    when(tokenGenerator.createToken(request)).thenReturn(token);
    when(resolver.resolve(token)).thenReturn(jwtToken);

    filter.doFilter(request, response, filterChain);

    verify(issuer, never()).authenticate(any(), any(), any());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void shouldRefreshIfRefreshable() throws IOException, ServletException {
    BearerToken token = createValidToken();
    JwtAccessToken jwtToken = mock(JwtAccessToken.class);
    JwtAccessToken newJwtToken = mock(JwtAccessToken.class);
    when(tokenGenerator.createToken(request)).thenReturn(token);
    when(resolver.resolve(token)).thenReturn(jwtToken);
    when(refresher.refresh(jwtToken)).thenReturn(of(newJwtToken));
    when(jwtToken.getExpiration()).thenReturn(new Date());

    filter.doFilter(request, response, filterChain);

    verify(issuer).authenticate(request, response, newJwtToken);
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void shouldNotRefreshEndlessToken() throws IOException, ServletException {
    BearerToken token = createValidToken();
    when(tokenGenerator.createToken(request)).thenReturn(token);

    JwtAccessToken jwtToken = mock(JwtAccessToken.class);
    when(resolver.resolve(token)).thenReturn(jwtToken);

    filter.doFilter(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(refresher);
    verifyNoInteractions(issuer);
  }

  @Test
  void shouldTrackMetricIfTokenWasRefreshed() throws IOException, ServletException {
    BearerToken token = createValidToken();
    JwtAccessToken jwtToken = mock(JwtAccessToken.class);
    JwtAccessToken newJwtToken = mock(JwtAccessToken.class);
    when(tokenGenerator.createToken(request)).thenReturn(token);
    when(resolver.resolve(token)).thenReturn(jwtToken);
    when(refresher.refresh(jwtToken)).thenReturn(of(newJwtToken));
    when(jwtToken.getExpiration()).thenReturn(new Date());

    filter.doFilter(request, response, filterChain);

    assertThat(meterRegistry.getMeters()).hasSize(1);
    Meter.Id meterId = meterRegistry.getMeters().get(0).getId();
    assertThat(meterId.getName()).isEqualTo("scm.auth.token.refresh");
    assertThat(meterId.getTag("type")).isEqualTo("JWT");
  }

  BearerToken createValidToken() {
    return valueOf("some.jwt.token");
  }
}
