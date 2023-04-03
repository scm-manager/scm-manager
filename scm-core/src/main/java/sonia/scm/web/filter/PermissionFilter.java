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

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContext;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.spi.ScmProviderHttpServlet;
import sonia.scm.repository.spi.ScmProviderHttpServletDecorator;
import sonia.scm.security.Authentications;
import sonia.scm.security.ScmSecurityException;
import sonia.scm.util.HttpUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Abstract http filter to check repository permissions.
 *
 * @author Sebastian Sdorra
 */
public abstract class PermissionFilter extends ScmProviderHttpServletDecorator
{

  /** the logger for PermissionFilter */
  private static final Logger logger =
    LoggerFactory.getLogger(PermissionFilter.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new permission filter
   *
   * @param configuration global scm-manager configuration
   *
   * @since 1.21
   */
  protected PermissionFilter(ScmConfiguration configuration, ScmProviderHttpServlet delegate)
  {
    super(delegate);
    this.configuration = configuration;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns true if the current request is a write request.
   *
   *
   * @param request
   *
   * @return returns true if the current request is a write request
   */
  protected abstract boolean isWriteRequest(HttpServletRequest request);

  //~--- methods --------------------------------------------------------------

  /**
   * Checks the permission for the requested repository. If the user has enough
   * permission, then the filter chain is called.
   *
   *
   * @param request http request
   * @param response http response
   *
   * @throws IOException
   * @throws ServletException
   */
  @Override
  public void service(HttpServletRequest request,
                      HttpServletResponse response, Repository repository)
    throws IOException, ServletException
  {
    Subject subject = SecurityUtils.getSubject();

    try
    {
      boolean writeRequest = isWriteRequest(request);

      if (hasPermission(repository, writeRequest))
      {
        logger.trace("{} access to repository {} for user {} granted",
          getActionAsString(writeRequest), repository.getName(),
          getUserName(subject));

        super.service(request, response, repository);
      }
      else
      {
        logger.info("{} access to repository {} for user {} denied",
          getActionAsString(writeRequest), repository.getName(),
          getUserName(subject));

        sendAccessDenied(request, response);
      }
    }
    catch (ScmSecurityException | AuthorizationException ex)
    {
      logger.warn("user " + subject.getPrincipal() +  " has not enough permissions", ex);
      sendAccessDenied(request, response);
    }

  }

  /**
   * Sends a "not enough privileges" error back to client.
   *
   *
   * @param request http request
   * @param response http response
   *
   * @throws IOException
   */
  protected void sendNotEnoughPrivilegesError(HttpServletRequest request,
                                              HttpServletResponse response)
    throws IOException
  {
    response.sendError(HttpServletResponse.SC_FORBIDDEN);
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
    HttpUtil.sendUnauthorized(response, configuration.getRealmDescription());
  }

  /**
   * Send access denied to the servlet response.
   *
   * @param request current http request object
   * @param response current http response object
   *
   * @throws IOException
   */
  private void sendAccessDenied(HttpServletRequest request,
                                HttpServletResponse response)
    throws IOException
  {
    if (!Authentications.isAuthenticatedSubjectAnonymous())
    {
      sendNotEnoughPrivilegesError(request, response);
    }
    else
    {
      sendUnauthorizedError(request, response);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns action as string.
   *
   *
   * @param writeRequest true if the action is a write action
   *
   * @return action as string
   */
  private String getActionAsString(boolean writeRequest)
  {
    return writeRequest
      ? "write"
      : "read";
  }

  /**
   * Returns the username from the given subject or anonymous.
   *
   *
   * @param subject user subject
   *
   * @return username username from subject or anonymous
   */
  private Object getUserName(Subject subject)
  {
    Object principal = subject.getPrincipal();

    if (principal == null)
    {
      principal = SCMContext.USER_ANONYMOUS;
    }

    return principal;
  }

  /**
   * Returns true if the current user has the required permissions.
   *
   *
   * @param repository repository for the permissions check
   * @param writeRequest true if request is a write request
   *
   * @return true if the current user has the required permissions
   */
  public static boolean hasPermission(Repository repository, boolean writeRequest)
  {
    boolean permitted;

    if (writeRequest)
    {
      permitted = RepositoryPermissions.push(repository).isPermitted();
    }
    else
    {
      permitted = RepositoryPermissions.pull(repository).isPermitted();
    }

    return permitted;
  }

  //~--- fields ---------------------------------------------------------------

  /** scm-manager global configuration */
  private final ScmConfiguration configuration;
}
