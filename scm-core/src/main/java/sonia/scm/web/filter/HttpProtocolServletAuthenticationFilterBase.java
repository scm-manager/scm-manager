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

package sonia.scm.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.security.TokenExpiredException;
import sonia.scm.util.HttpUtil;
import sonia.scm.web.UserAgent;
import sonia.scm.web.UserAgentParser;
import sonia.scm.web.WebTokenGenerator;

import java.io.IOException;
import java.util.Set;

public class HttpProtocolServletAuthenticationFilterBase extends AuthenticationFilter {

  private final UserAgentParser userAgentParser;

  protected HttpProtocolServletAuthenticationFilterBase(
    ScmConfiguration configuration,
    Set<WebTokenGenerator> tokenGenerators,
    UserAgentParser userAgentParser) {
    super(configuration, tokenGenerators);
    this.userAgentParser = userAgentParser;
  }

  @Override
  protected void handleUnauthorized(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
    UserAgent userAgent = userAgentParser.parse(request);
    if (userAgent.isBrowser()) {
      // we can proceed the filter chain because the HttpProtocolServlet will render the ui if the client is a browser
      chain.doFilter(request, response);
    } else {
      HttpUtil.sendUnauthorized(request, response, configuration.getRealmDescription());
    }
  }

  @Override
  protected void handleTokenExpiredException(HttpServletRequest request, HttpServletResponse response, FilterChain chain, TokenExpiredException tokenExpiredException) throws IOException, ServletException {
    UserAgent userAgent = userAgentParser.parse(request);
    if (userAgent.isBrowser()) {
      // we can proceed the filter chain because the HttpProtocolServlet will render the ui if the client is a browser
      chain.doFilter(request, response);
    } else {
      super.handleTokenExpiredException(request, response, chain, tokenExpiredException);
    }
  }
}
