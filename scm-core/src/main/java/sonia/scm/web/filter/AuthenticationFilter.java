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

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.Priorities;
import sonia.scm.SCMContext;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.security.AnonymousMode;
import sonia.scm.security.AnonymousToken;
import sonia.scm.security.TokenExpiredException;
import sonia.scm.security.TokenValidationFailedException;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.Util;
import sonia.scm.web.WebTokenGenerator;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Handles authentication, if a one of the {@link WebTokenGenerator} returns
 * an {@link AuthenticationToken}.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
@Singleton
public class AuthenticationFilter extends HttpFilter {

  private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

  /**
   * marker for failed authentication
   */
  private static final String ATTRIBUTE_FAILED_AUTH = "sonia.scm.auth.failed";


  private final List<WebTokenGenerator> tokenGenerators;
  protected ScmConfiguration configuration;

  /**
   * Constructs a new basic authenticaton filter.
   *
   * @param configuration   scm-manager global configuration
   * @param tokenGenerators web token generators
   */
  @Inject
  public AuthenticationFilter(ScmConfiguration configuration, Set<WebTokenGenerator> tokenGenerators) {
    this.configuration = configuration;
    this.tokenGenerators = Priorities.sortInstances(tokenGenerators);
  }

  /**
   * Handles authentication, if a one of the {@link WebTokenGenerator} returns
   * an {@link AuthenticationToken}.
   *
   * @param request  servlet request
   * @param response servlet response
   * @param chain    filter chain
   * @throws IOException
   * @throws ServletException
   */
  @Override
  protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
    throws IOException, ServletException {
    Subject subject = SecurityUtils.getSubject();

    AuthenticationToken token = createToken(request);

    if (token != null) {
      logger.trace(
        "found authentication token on request, start authentication");
      handleAuthentication(request, response, chain, subject, token);
    } else if (subject.isAuthenticated()) {
      logger.trace("user is already authenticated");
      processChain(request, response, chain, subject);
    } else if (isAnonymousAccessEnabled()) {
      logger.trace("anonymous access granted");
      subject.login(new AnonymousToken());
      processChain(request, response, chain, subject);
    } else {
      logger.trace("could not find user send unauthorized");
      handleUnauthorized(request, response, chain);
    }
  }

  /**
   * Sends status code 401 back to client, if the authentication has failed.
   * In all other cases the method will send status code 403 back to client.
   *
   * @param request  servlet request
   * @param response servlet response
   * @param chain    filter chain
   * @throws IOException
   * @throws ServletException
   * @since 1.8
   */
  protected void handleUnauthorized(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain chain)
    throws IOException, ServletException {

    // send only forbidden, if the authentication has failed.
    // see https://bitbucket.org/sdorra/scm-manager/issue/545/git-clone-with-username-in-url-does-not
    if (Boolean.TRUE.equals(request.getAttribute(ATTRIBUTE_FAILED_AUTH))) {
      sendFailedAuthenticationError(request, response);
    } else {
      sendUnauthorizedError(request, response);
    }
  }

  /**
   * Sends an error for a failed authentication back to client.
   *
   * @param request  http request
   * @param response http response
   * @throws IOException
   */
  protected void sendFailedAuthenticationError(HttpServletRequest request,
                                               HttpServletResponse response)
    throws IOException {
    HttpUtil.sendUnauthorized(request, response,
      configuration.getRealmDescription());
  }

  /**
   * Sends an unauthorized error back to client.
   *
   * @param request  http request
   * @param response http response
   * @throws IOException
   */
  protected void sendUnauthorizedError(HttpServletRequest request, HttpServletResponse response) throws IOException {
    HttpUtil.sendUnauthorized(request, response, configuration.getRealmDescription());
  }

  protected void handleTokenExpiredException(HttpServletRequest request, HttpServletResponse response,
                                             FilterChain chain, TokenExpiredException tokenExpiredException) throws IOException, ServletException {
    logger.trace("rethrow token expired exception");
    throw tokenExpiredException;
  }

  protected void handleTokenValidationFailedException(HttpServletRequest request, HttpServletResponse response, FilterChain chain, TokenValidationFailedException tokenValidationFailedException) throws IOException, ServletException {
    logger.trace("send unauthorized, because of a failed token validation");
    handleUnauthorized(request, response, chain);
  }

  /**
   * Iterates all {@link WebTokenGenerator} and creates an
   * {@link AuthenticationToken} from the given request.
   *
   * @param request http servlet request
   * @return authentication token of {@code null}
   */
  private AuthenticationToken createToken(HttpServletRequest request) {
    AuthenticationToken token = null;
    for (WebTokenGenerator generator : tokenGenerators) {
      token = generator.createToken(request);

      if (token != null) {
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
   * @param request  http servlet request
   * @param response http servlet response
   * @param chain    filter chain
   * @param subject  subject
   * @param token    authentication token
   * @throws IOException
   * @throws ServletException
   */
  private void handleAuthentication(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain chain, Subject subject,
                                    AuthenticationToken token)
    throws IOException, ServletException {
    logger.trace("found basic authorization header, start authentication");

    try {
      subject.login(token);
      processChain(request, response, chain, subject);
    } catch (TokenExpiredException ex) {
      // Rethrow to be caught by TokenExpiredFilter
      logger.trace("handle token expired exception");
      handleTokenExpiredException(request, response, chain, ex);
    } catch (TokenValidationFailedException ex) {
      logger.trace("handle token validation failed exception");
      handleTokenValidationFailedException(request, response, chain, ex);
    } catch (AuthenticationException ex) {
      logger.warn("authentication failed", ex);
      handleUnauthorized(request, response, chain);
    }
  }

  /**
   * Process the filter chain.
   *
   * @param request  http servlet request
   * @param response http servlet response
   * @param chain    filter chain
   * @param subject  subject
   * @throws IOException
   * @throws ServletException
   */
  private void processChain(HttpServletRequest request,
                            HttpServletResponse response, FilterChain chain, Subject subject)
    throws IOException, ServletException {
    String username = Util.EMPTY_STRING;

    if (!subject.isAuthenticated()) {

      // anonymous access
      username = SCMContext.USER_ANONYMOUS;
    } else {
      Object obj = subject.getPrincipal();

      if (obj != null) {
        username = obj.toString();
      }
    }

    chain.doFilter(new PropagatePrincipleServletRequestWrapper(request, username),
      response);
  }

  /**
   * Returns {@code true} if anonymous access is enabled.
   *
   * @return {@code true} if anonymous access is enabled
   */
  protected boolean isAnonymousAccessEnabled() {
    return (configuration != null) && configuration.getAnonymousMode() != AnonymousMode.OFF;
  }
}
