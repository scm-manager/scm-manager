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
