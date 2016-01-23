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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import java.io.IOException;
import java.util.UUID;
import javax.inject.Singleton;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.util.HttpUtil;
import sonia.scm.web.filter.HttpFilter;

/**
 * Xsrf protection http filter. The filter will issue an cookie with an xsrf protection token on the first ajax request
 * of the scm web interface and marks the http session as xsrf protected. On every other request within a protected 
 * session, the web interface has to send the token from the cookie as http header on every request. If the filter 
 * receives an request to a protected session, without proper xsrf header the filter will abort the request and send an
 * http error code back to the client. If the filter receives an request to a non protected session, from a non web 
 * interface client the filter will call the chain.
 * 
 * TODO for scm-manager 2 we have to store the csrf token as part of the jwt token instead of session.
 * 
 * @see https://bitbucket.org/sdorra/scm-manager/issues/793/json-hijacking-vulnerability-cwe-116-cwe
 * @author Sebastian Sdorra
 * @version 1.47
 */
@Singleton
public final class XsrfProtectionFilter extends HttpFilter
{
  
  /**
   * the logger for XsrfProtectionFilter
   */
  private static final Logger logger = LoggerFactory.getLogger(XsrfProtectionFilter.class);
  
  /**
   * Key used for session, header and cookie.
   */
  static final String KEY = "X-XSRF-Token";
  
  @Override
  protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws
    IOException, ServletException
  {
    HttpSession session = request.getSession(true);
    String storedToken = (String) session.getAttribute(KEY);
    if ( ! Strings.isNullOrEmpty(storedToken) ){
      String headerToken = request.getHeader(KEY);
      if ( storedToken.equals(headerToken) ){
        logger.trace("received valid xsrf protected request");
        chain.doFilter(request, response);
      } else {
        // is forbidden the correct status code?
        logger.warn("received request to a xsrf protected session without proper xsrf token");
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
      }
    } else if (HttpUtil.isWUIRequest(request)) {
      logger.debug("received wui request, mark session as xsrf protected and issue a new token");
      String token = createToken();
      session.setAttribute(KEY, token);
      XsrfCookies.create(request, response, token);
      chain.doFilter(request, response);
    } else {
      // handle non webinterface clients, which does not need xsrf protection
      logger.trace("received request to a non xsrf protected session");
      chain.doFilter(request, response);
    }
  }
  
  private String createToken()
  {
    // TODO create interface and use a better method
    return UUID.randomUUID().toString();
  }
  
}
