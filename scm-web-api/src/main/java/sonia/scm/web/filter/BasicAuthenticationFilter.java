/**
 * Copyright (c) 2010, Sebastian Sdorra
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

package sonia.scm.web.filter;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import sonia.scm.user.User;
import sonia.scm.util.Util;
import sonia.scm.web.security.WebSecurityContext;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.core.util.Base64;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class BasicAuthenticationFilter extends HttpFilter
{

  /** Field description */
  public static final String AUTHENTICATION_REALM = "SONIA :: SCM Manager";

  /** Field description */
  public static final String AUTHORIZATION_BASIC_PREFIX = "BASIC";

  /** Field description */
  public static final String CREDENTIAL_SEPARATOR = ":";

  /** Field description */
  public static final String HEADERVALUE_CONNECTION_CLOSE = "close";

  /** Field description */
  public static final String HEADER_AUTHORIZATION = "Authorization";

  /** Field description */
  public static final String HEADER_CONNECTION = "connection";

  /** Field description */
  public static final String HEADER_WWW_AUTHENTICATE = "WWW-Authenticate";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param securityContextProvider
   */
  @Inject
  public BasicAuthenticationFilter(
          Provider<WebSecurityContext> securityContextProvider)
  {
    this.securityContextProvider = securityContextProvider;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   * @param chain
   *
   * @throws IOException
   * @throws ServletException
   */
  @Override
  protected void doFilter(HttpServletRequest request,
                          HttpServletResponse response, FilterChain chain)
          throws IOException, ServletException
  {
    WebSecurityContext securityContext = securityContextProvider.get();
    User user = null;

    if (securityContext != null)
    {
      if (!securityContext.isAuthenticated())
      {
        String authentication = request.getHeader(HEADER_AUTHORIZATION);

        if (Util.isEmpty(authentication))
        {
          sendUnauthorized(response);
        }
        else
        {
          if (!authentication.toUpperCase().startsWith(
                  AUTHORIZATION_BASIC_PREFIX))
          {
            throw new ServletException("wrong basic header");
          }

          String token = authentication.substring(6);

          token = new String(Base64.decode(token.getBytes()));

          String[] credentials = token.split(CREDENTIAL_SEPARATOR);

          user = securityContext.authenticate(request, response,
                  credentials[0], credentials[1]);
        }
      }
      else
      {
        user = securityContext.getUser();
      }
    }

    if (user != null)
    {
      chain.doFilter(new SecurityHttpServletRequestWrapper(request, user),
                     response);
    }
    else
    {
      sendUnauthorized(response);
    }
  }

  /**
   * Method description
   *
   *
   * @param response
   */
  private void sendUnauthorized(HttpServletResponse response)
  {
    response.setHeader(HEADER_WWW_AUTHENTICATE,
                       "Basic realm=\"" + AUTHENTICATION_REALM + "\"");
    response.setHeader(HEADER_CONNECTION, HEADERVALUE_CONNECTION_CLOSE);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Provider<WebSecurityContext> securityContextProvider;
}
