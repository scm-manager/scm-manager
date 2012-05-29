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

import com.google.common.base.Splitter;
import com.google.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.SCMContext;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.PermissionType;
import sonia.scm.repository.PermissionUtil;
import sonia.scm.repository.Repository;
import sonia.scm.security.ScmSecurityException;
import sonia.scm.user.User;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.HttpUtil;
import sonia.scm.util.Util;
import sonia.scm.web.security.WebSecurityContext;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Iterator;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class PermissionFilter extends HttpFilter
{

  /** the logger for PermissionFilter */
  private static final Logger logger =
    LoggerFactory.getLogger(PermissionFilter.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param configuration
   * @param securityContextProvider
   */
  public PermissionFilter(ScmConfiguration configuration,
                          Provider<WebSecurityContext> securityContextProvider)
  {
    this.configuration = configuration;
    this.securityContextProvider = securityContextProvider;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   */
  protected abstract Repository getRepository(HttpServletRequest request);

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   */
  protected abstract boolean isWriteRequest(HttpServletRequest request);

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

    AssertUtil.assertIsNotNull(securityContext);

    User user = securityContext.getUser();

    if (user != null)
    {
      try
      {
        Repository repository = getRepository(request);

        if (repository != null)
        {
          boolean writeRequest = isWriteRequest(request);

          if (hasPermission(repository, securityContext, writeRequest))
          {
            chain.doFilter(request, response);
          }
          else
          {
            if (logger.isInfoEnabled())
            {
              logger.info("{} access to repository {} for user {} denied",
                          new Object[] { writeRequest
                                         ? "write"
                                         : "read", repository.getName(),
                                         user.getName() });
            }

            sendAccessDenied(response, user);
          }
        }
        else
        {
          if (logger.isDebugEnabled())
          {
            logger.debug("repository not found");
          }

          response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
      }
      catch (IllegalStateException ex)
      {
        if (logger.isWarnEnabled())
        {
          logger.warn(
              "wrong request at ".concat(request.getRequestURI()).concat(
                " send redirect"), ex);
        }

        response.sendRedirect(getRepositoryRootHelpUrl(request));
      }
      catch (ScmSecurityException ex)
      {
        if (logger.isWarnEnabled())
        {
          logger.warn("user {} has not enough permissions", user.getName());
        }

        sendAccessDenied(response, user);
      }
    }
    else
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("user in not authenticated");
      }

      response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }
  }

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   */
  private String extractType(HttpServletRequest request)
  {
    Iterator<String> it = Splitter.on(
                              HttpUtil.SEPARATOR_PATH).omitEmptyStrings().split(
                              request.getRequestURI()).iterator();
    String type = it.next();

    if (Util.isNotEmpty(request.getContextPath()))
    {
      type = it.next();
    }

    return type;
  }

  /**
   * Method description
   *
   *
   * @param response
   * @param user
   *
   * @throws IOException
   */
  private void sendAccessDenied(HttpServletResponse response, User user)
          throws IOException
  {
    if (SCMContext.USER_ANONYMOUS.equals(user.getName()))
    {
      HttpUtil.sendUnauthorized(response);
    }
    else
    {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   */
  private String getRepositoryRootHelpUrl(HttpServletRequest request)
  {
    String type = extractType(request);
    String helpUrl = HttpUtil.getCompleteUrl(request,
                       "/api/rest/help/repository-root/");

    helpUrl = helpUrl.concat(type).concat(".html");

    return helpUrl;
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param securityContext
   * @param writeRequest
   *
   * @return
   */
  private boolean hasPermission(Repository repository,
                                WebSecurityContext securityContext,
                                boolean writeRequest)
  {
    boolean permitted = false;

    if (writeRequest)
    {
      permitted = PermissionUtil.isWritable(configuration, repository,
              securityContext);
    }
    else
    {
      permitted = PermissionUtil.hasPermission(repository, securityContext,
              PermissionType.READ);
    }

    return permitted;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected Provider<WebSecurityContext> securityContextProvider;

  /** Field description */
  private ScmConfiguration configuration;
}
