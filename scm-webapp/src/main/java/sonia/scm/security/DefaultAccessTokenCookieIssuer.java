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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.initialization.InitializationWebTokenGenerator;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.Util;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Generates cookies and invalidates access token cookies.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
public final class DefaultAccessTokenCookieIssuer implements AccessTokenCookieIssuer {

  /**
   * the logger for DefaultAccessTokenCookieIssuer
   */
  private static final Logger LOG = LoggerFactory.getLogger(DefaultAccessTokenCookieIssuer.class);

  private static final int DEFAULT_COOKIE_EXPIRATION_AMOUNT = 365;
  private static final TimeUnit DEFAULT_COOKIE_EXPIRATION_UNIT = TimeUnit.DAYS;
  private static final int DEFAULT_COOKIE_EXPIRATION = (int) TimeUnit.SECONDS.convert(DEFAULT_COOKIE_EXPIRATION_AMOUNT, DEFAULT_COOKIE_EXPIRATION_UNIT);

  private final ScmConfiguration configuration;

  /**
   * Constructs a new instance.
   *
   * @param configuration scm main configuration
   */
  @Inject
  public DefaultAccessTokenCookieIssuer(ScmConfiguration configuration) {
    this.configuration = configuration;
  }

  /**
   * Creates a cookie for token authentication and attaches it to the response.
   *
   * @param request http servlet request
   * @param response http servlet response
   * @param accessToken access token
   */
  public void authenticate(HttpServletRequest request, HttpServletResponse response, AccessToken accessToken) {
    LOG.trace("create and attach cookie for access token {}", accessToken.getId());
    Cookie c = new Cookie(HttpUtil.COOKIE_BEARER_AUTHENTICATION, accessToken.compact());
    c.setPath(contextPath(request));
    c.setMaxAge(DEFAULT_COOKIE_EXPIRATION);
    c.setHttpOnly(isHttpOnly());
    c.setSecure(isSecure(request));

    // attach cookie to response
    response.addCookie(c);
  }

  /**
   * Invalidates the authentication cookie.
   *
   * @param request http servlet request
   * @param response http servlet response
   */
  public void invalidate(HttpServletRequest request, HttpServletResponse response) {
    LOG.trace("invalidates access token cookie");
    invalidateCookie(request, response, HttpUtil.COOKIE_BEARER_AUTHENTICATION);

    if (Arrays.stream(request.getCookies()).anyMatch(cookie -> cookie.getName().equals(InitializationWebTokenGenerator.INIT_TOKEN_HEADER))) {
      LOG.trace("invalidates initialization token cookie");
      invalidateInitTokenCookie(request, response);
    }
  }

  private void invalidateInitTokenCookie(HttpServletRequest request, HttpServletResponse response) {
    invalidateCookie(request, response, InitializationWebTokenGenerator.INIT_TOKEN_HEADER);
  }

  private void invalidateCookie(HttpServletRequest request, HttpServletResponse response, String cookieBearerAuthentication) {
    Cookie c = new Cookie(cookieBearerAuthentication, Util.EMPTY_STRING);
    c.setPath(contextPath(request));
    c.setMaxAge(0);
    c.setHttpOnly(isHttpOnly());
    c.setSecure(isSecure(request));

    // attach empty cookie, that the browser can remove it
    response.addCookie(c);
  }

  @VisibleForTesting
  String contextPath(HttpServletRequest request) {
    String contextPath = request.getContextPath();
    if (Strings.isNullOrEmpty(contextPath)) {
      return "/";
    }
    return contextPath;
  }

  private boolean isSecure(HttpServletRequest request){
    boolean secure = request.isSecure();
    if (!secure) {
      LOG.warn("issuet a non secure cookie, protect your scm-manager instance with tls https://scm-manager.org/docs/latest/en/administration/scm-server/");
    }
    return secure;
  }

  private boolean isHttpOnly(){
    // set http only flag only xsrf protection is disabled,
    // because we have to extract the xsrf key with javascript in the wui
    return !configuration.isEnabledXsrfProtection();
  }

}
