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



package sonia.scm.filter;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import sonia.scm.Priority;
import sonia.scm.SCMContext;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.web.filter.HttpFilter;
import sonia.scm.web.filter.SecurityHttpServletRequestWrapper;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
@Priority(Filters.PRIORITY_AUTHORIZATION)
@WebElement(value = Filters.PATTERN_RESTAPI, morePatterns = { Filters.PATTERN_DEBUG })
public class SecurityFilter extends HttpFilter
{

  /** name of request attribute for the primary principal */
  @VisibleForTesting
  static final String ATTRIBUTE_REMOTE_USER = "principal";

  /** Field description */
  public static final String URL_AUTHENTICATION = "/api/rest/auth";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param configuration
   */
  @Inject
  public SecurityFilter(ScmConfiguration configuration)
  {
    this.configuration = configuration;
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
    String uri =
      request.getRequestURI().substring(request.getContextPath().length());

    if (!uri.startsWith(URL_AUTHENTICATION))
    {
      Subject subject = SecurityUtils.getSubject();
      if (hasPermission(subject))
      {
        // add primary principal as request attribute
        // see https://goo.gl/JRjNmf
        String username = getUsername(subject);
        request.setAttribute(ATTRIBUTE_REMOTE_USER, username);

        // wrap servlet request to provide authentication informations
        chain.doFilter(new SecurityHttpServletRequestWrapper(request, username), response);
      }
      else if (subject.isAuthenticated() || subject.isRemembered())
      {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
      }
      else if (configuration.isAnonymousAccessEnabled())
      {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
      }
      else
      {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      }
    }
    else
    {
      chain.doFilter(request, response);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param subject
   *
   * @return
   */
  protected boolean hasPermission(Subject subject)
  {
    return ((configuration != null)
      && configuration.isAnonymousAccessEnabled()) || subject.isAuthenticated()
        || subject.isRemembered();
  }

  private String getUsername(Subject subject)
  {
    String username = SCMContext.USER_ANONYMOUS;

    if (subject.isAuthenticated() || subject.isRemembered())
    {
      Object obj = subject.getPrincipal();

      if (obj != null)
      {
        username = obj.toString();
      }
    }

    return username;
  }

  //~--- fields ---------------------------------------------------------------

  /** scm configuration */
  private final ScmConfiguration configuration;
}
