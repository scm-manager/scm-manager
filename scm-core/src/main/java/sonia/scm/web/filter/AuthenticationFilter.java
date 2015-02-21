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
import com.google.inject.Singleton;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.SCMContext;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.Util;
import sonia.scm.web.WebTokenGenerator;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles authentication, if a one of the {@link WebTokenGenerator} returns
 * an {@link AuthenticationToken}.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
@Singleton
public class AuthenticationFilter extends HttpFilter
{

  /** marker for failed authentication */
  private static final String ATTRIBUTE_FAILED_AUTH = "sonia.scm.auth.failed";

  /** Field description */
  private static final String HEADER_AUTHORIZATION = "Authorization";

  /** the logger for AuthenticationFilter */
  private static final Logger logger =
    LoggerFactory.getLogger(AuthenticationFilter.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new basic authenticaton filter.
   *
   * @param configuration scm-manager global configuration
   * @param tokenGenerators web token generators
   */
  @Inject
  public AuthenticationFilter(ScmConfiguration configuration,
    Set<WebTokenGenerator> tokenGenerators)
  {
    this.configuration = configuration;
    this.tokenGenerators = tokenGenerators;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Handles authentication, if a one of the {@link WebTokenGenerator} returns
   * an {@link AuthenticationToken}.
   *
   *
   * @param request servlet request
   * @param response servlet response
   * @param chain filter chain
   *
   * @throws IOException
   * @throws ServletException
   */
  @Override
  protected void doFilter(HttpServletRequest request,
    HttpServletResponse response, FilterChain chain)
    throws IOException, ServletException
  {
    Subject subject = SecurityUtils.getSubject();

    AuthenticationToken token = createToken(request);

    if (token != null)
    {
      logger.trace(
        "found authentication token on request, start authentication");
      handleAuthentication(request, response, chain, subject, token);
    }
    else if (subject.isAuthenticated())
    {
      logger.trace("user is allready authenticated");
      processChain(request, response, chain, subject);
    }
    else if (isAnonymousAccessEnabled())
    {
      logger.trace("anonymous access granted");
      processChain(request, response, chain, subject);
    }
    else
    {
      logger.trace("could not find user send unauthorized");
      handleUnauthorized(request, response, chain);
    }
  }

  /**
   * Sends status code 403 back to client, if the authentication has failed.
   * In all other cases the method will send status code 403 back to client.
   *
   * @param request servlet request
   * @param response servlet response
   * @param chain filter chain
   *
   * @throws IOException
   * @throws ServletException
   *
   * @since 1.8
   */
  protected void handleUnauthorized(HttpServletRequest request,
    HttpServletResponse response, FilterChain chain)
    throws IOException, ServletException
  {

    // send only forbidden, if the authentication has failed.
    // see https://bitbucket.org/sdorra/scm-manager/issue/545/git-clone-with-username-in-url-does-not
    if (Boolean.TRUE.equals(request.getAttribute(ATTRIBUTE_FAILED_AUTH)))
    {
      sendFailedAuthenticationError(request, response);
    }
    else
    {
      sendUnauthorizedError(request, response);
    }
  }

  /**
   * Sends an error for a failed authentication back to client.
   *
   *
   * @param request http request
   * @param response http response
   *
   * @throws IOException
   */
  protected void sendFailedAuthenticationError(HttpServletRequest request,
    HttpServletResponse response)
    throws IOException
  {
    HttpUtil.sendUnauthorized(request, response,
      configuration.getRealmDescription());
  }

  /**
   * Sends an unauthorized error back to client.
   *
   *
   * @param request http request
   * @param response http response
   *
   * @throws IOException
   */
  protected void sendUnauthorizedError(HttpServletRequest request,
    HttpServletResponse response)
    throws IOException
  {
    HttpUtil.sendUnauthorized(request, response,
      configuration.getRealmDescription());
  }

  /**
   * Iterates all {@link WebTokenGenerator} and creates an
   * {@link AuthenticationToken} from the given request.
   *
   *
   * @param request http servlet request
   *
   * @return authentication token of {@code null}
   */
  private AuthenticationToken createToken(HttpServletRequest request)
  {
    AuthenticationToken token = null;

    for (WebTokenGenerator generator : tokenGenerators)
    {
      token = generator.createToken(request);

      if (token != null)
      {
        logger.trace("generated web token {} from generator {}",
          token.getClass(), generator.getClass());

        break;
      }
    }

    return token;
  }

  /**
   * Handle authentication with the given {@link AuthenticationToken}.
   *
   *
   * @param request http servlet request
   * @param response http servlet response
   * @param chain filter chain
   * @param subject subject
   * @param token authentication token
   *
   * @throws IOException
   * @throws ServletException
   */
  private void handleAuthentication(HttpServletRequest request,
    HttpServletResponse response, FilterChain chain, Subject subject,
    AuthenticationToken token)
    throws IOException, ServletException
  {
    logger.trace("found basic authorization header, start authentication");

    try
    {
      subject.login(token);
      processChain(request, response, chain, subject);
    }
    catch (AuthenticationException ex)
    {
      logger.warn("authentication failed", ex);
      handleUnauthorized(request, response, chain);
    }
  }

  /**
   * Process the filter chain.
   *
   *
   * @param request http servlet request
   * @param response http servlet response
   * @param chain filter chain
   * @param subject subject
   *
   * @throws IOException
   * @throws ServletException
   */
  private void processChain(HttpServletRequest request,
    HttpServletResponse response, FilterChain chain, Subject subject)
    throws IOException, ServletException
  {
    String username = Util.EMPTY_STRING;

    if (!subject.isAuthenticated())
    {

      // anonymous access
      username = SCMContext.USER_ANONYMOUS;
    }
    else
    {
      Object obj = subject.getPrincipal();

      if (obj != null)
      {
        username = obj.toString();
      }
    }

    chain.doFilter(new SecurityHttpServletRequestWrapper(request, username),
      response);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns {@code true} if anonymous access is enabled.
   *
   *
   * @return {@code true} if anonymous access is enabled
   */
  private boolean isAnonymousAccessEnabled()
  {
    return (configuration != null) && configuration.isAnonymousAccessEnabled();
  }

  //~--- fields ---------------------------------------------------------------

  /** set of web token generators */
  private final Set<WebTokenGenerator> tokenGenerators;

  /** scm main configuration */
  protected ScmConfiguration configuration;
}
