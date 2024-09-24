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

package sonia.scm.web.security;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.inject.Inject;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.Priority;
import sonia.scm.filter.Filters;
import sonia.scm.filter.WebElement;
import sonia.scm.metrics.AuthenticationMetrics;
import sonia.scm.security.AccessToken;
import sonia.scm.security.AccessTokenCookieIssuer;
import sonia.scm.security.AccessTokenResolver;
import sonia.scm.security.BearerToken;
import sonia.scm.security.JwtAccessToken;
import sonia.scm.security.JwtAccessTokenRefresher;
import sonia.scm.web.WebTokenGenerator;
import sonia.scm.web.filter.HttpFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.empty;
import static java.util.Optional.of;

@Priority(Filters.PRIORITY_POST_AUTHENTICATION)
@WebElement(value = Filters.PATTERN_RESTAPI,
  morePatterns = {Filters.PATTERN_DEBUG})
public class TokenRefreshFilter extends HttpFilter {

  private static final Logger LOG = LoggerFactory.getLogger(TokenRefreshFilter.class);

  private final Set<WebTokenGenerator> tokenGenerators;
  private final JwtAccessTokenRefresher refresher;
  private final AccessTokenResolver resolver;
  private final AccessTokenCookieIssuer issuer;
  private final Counter tokenRefreshCounter;

  @Inject
  public TokenRefreshFilter(Set<WebTokenGenerator> tokenGenerators, JwtAccessTokenRefresher refresher, AccessTokenResolver resolver, AccessTokenCookieIssuer issuer, MeterRegistry meterRegistry) {
    this.tokenGenerators = tokenGenerators;
    this.refresher = refresher;
    this.resolver = resolver;
    this.issuer = issuer;
    this.tokenRefreshCounter = AuthenticationMetrics.tokenRefresh(meterRegistry, "JWT");
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
    if (!token.getCredentials().contains(".")) {
      LOG.trace("Ignoring token without dot. This probably is an API key, no JWT");
      return;
    }
    try {
      accessToken = resolver.resolve(token);
    } catch (AuthenticationException e) {
      LOG.trace("could not resolve token", e);
      return;
    }
    if (accessToken instanceof JwtAccessToken && !isEndlessToken((JwtAccessToken) accessToken)) {
      refresher.refresh((JwtAccessToken) accessToken)
        .ifPresent(jwtAccessToken -> refreshJwtToken(request, response, jwtAccessToken));
    }
  }

  private void refreshJwtToken(HttpServletRequest request, HttpServletResponse response, JwtAccessToken jwtAccessToken) {
    tokenRefreshCounter.increment();
    LOG.debug("refreshing JWT authentication token");
    issuer.authenticate(request, response, jwtAccessToken);
  }

  private boolean isEndlessToken(JwtAccessToken token) {
    return token.getExpiration() == null;
  }
}
