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

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.Priority;
import sonia.scm.filter.Filters;
import sonia.scm.filter.WebElement;
import sonia.scm.security.AccessToken;
import sonia.scm.security.AccessTokenCookieIssuer;
import sonia.scm.security.AccessTokenResolver;
import sonia.scm.security.BearerToken;
import sonia.scm.security.JwtAccessToken;
import sonia.scm.security.JwtAccessTokenRefresher;
import sonia.scm.web.WebTokenGenerator;
import sonia.scm.web.filter.HttpFilter;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.empty;
import static java.util.Optional.of;

@Priority(Filters.PRIORITY_POST_AUTHENTICATION)
@WebElement(value = Filters.PATTERN_RESTAPI,
  morePatterns = { Filters.PATTERN_DEBUG })
public class TokenRefreshFilter extends HttpFilter {

  private static final Logger LOG = LoggerFactory.getLogger(TokenRefreshFilter.class);

  private final Set<WebTokenGenerator> tokenGenerators;
  private final JwtAccessTokenRefresher refresher;
  private final AccessTokenResolver resolver;
  private final AccessTokenCookieIssuer issuer;

  @Inject
  public TokenRefreshFilter(Set<WebTokenGenerator> tokenGenerators, JwtAccessTokenRefresher refresher, AccessTokenResolver resolver, AccessTokenCookieIssuer issuer) {
    this.tokenGenerators = tokenGenerators;
    this.refresher = refresher;
    this.resolver = resolver;
    this.issuer = issuer;
  }

  @Override
  protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
    extractToken(request).ifPresent(token -> examineToken(request, response, token));
    chain.doFilter(request, response);
  }

  private Optional<BearerToken> extractToken(HttpServletRequest request) {
    for (WebTokenGenerator generator : tokenGenerators) {
      AuthenticationToken token = generator.createToken(request);
      if (token instanceof BearerToken) {
        return of((BearerToken) token);
      }
    }
    return empty();
  }

  private void examineToken(HttpServletRequest request, HttpServletResponse response, BearerToken token) {
    AccessToken accessToken;
    try {
      accessToken = resolver.resolve(token);
    } catch (AuthenticationException e) {
      LOG.trace("could not resolve token", e);
      return;
    }
    if (accessToken instanceof JwtAccessToken) {
      refresher.refresh((JwtAccessToken) accessToken)
        .ifPresent(jwtAccessToken -> refreshToken(request, response, jwtAccessToken));
    }
  }

  private void refreshToken(HttpServletRequest request, HttpServletResponse response, JwtAccessToken jwtAccessToken) {
    LOG.debug("refreshing authentication token");
    issuer.authenticate(request, response, jwtAccessToken);
  }
}
