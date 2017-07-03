/**
 * Copyright (c) 2014, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */
package sonia.scm.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.Util;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Generates cookies and invalidates access token cookies.
 * 
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
public final class AccessTokenCookieIssuer {
  
  /**
   * the logger for AccessTokenCookieIssuer
   */
  private static final Logger LOG = LoggerFactory.getLogger(AccessTokenCookieIssuer.class);
  
  private final ScmConfiguration configuration;

  /**
   * Constructs a new instance.
   * 
   * @param configuration scm main configuration
   */
  @Inject
  public AccessTokenCookieIssuer(ScmConfiguration configuration) {
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
    c.setPath(request.getContextPath());
    c.setMaxAge(getMaxAge(accessToken));
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
    
    Cookie c = new Cookie(HttpUtil.COOKIE_BEARER_AUTHENTICATION, Util.EMPTY_STRING);
    c.setPath(request.getContextPath());
    c.setMaxAge(0);
    c.setHttpOnly(isHttpOnly());
    c.setSecure(isSecure(request));
    
    // attach empty cookie, that the browser can remove it
    response.addCookie(c);
  }
  
  private int getMaxAge(AccessToken accessToken){
    long maxAgeMs = accessToken.getExpiration().getTime() - new Date().getTime();
    return (int) TimeUnit.MILLISECONDS.toSeconds(maxAgeMs);
  }
  
  private boolean isSecure(HttpServletRequest request){
    boolean secure = request.isSecure();
    if (!secure) {
      LOG.warn("issuet a non secure cookie, protect your scm-manager instance with tls https://goo.gl/lVm0ph");
    }
    return secure;
  }
  
  private boolean isHttpOnly(){
    // set http only flag only xsrf protection is disabled,
    // because we have to extract the xsrf key with javascript in the wui
    return !configuration.isEnabledXsrfProtection();
  }
  
}
