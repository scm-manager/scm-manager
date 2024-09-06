/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.web.filter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

import java.io.IOException;

/**
 * Abstract http filter to check repository permissions.
 *
 */
public abstract class PermissionFilter extends ScmProviderHttpServletDecorator {

  private static final Logger LOG = LoggerFactory.getLogger(PermissionFilter.class);
  private final ScmConfiguration configuration;

  /**
   * @since 1.21
   */
  protected PermissionFilter(ScmConfiguration configuration, ScmProviderHttpServlet delegate) {
    super(delegate);
    this.configuration = configuration;
  }

  /**
   * Returns true if the current request is a write request.
   *
   * @param request
   * @return returns true if the current request is a write request
   */
  protected abstract boolean isWriteRequest(HttpServletRequest request);

  /**
   * Checks the permission for the requested repository. If the user has enough
   * permission, then the filter chain is called.
   *
   * @param request  http request
   * @param response http response
   * @throws IOException
   * @throws ServletException
   */
  @Override
  public void service(HttpServletRequest request, HttpServletResponse response, Repository repository)
    throws IOException, ServletException {
    Subject subject = SecurityUtils.getSubject();

    try {
      boolean writeRequest = isWriteRequest(request);

      if (hasPermission(repository, writeRequest)) {
        LOG.trace("{} access to repository {} for user {} granted",
          getActionAsString(writeRequest), repository.getName(),
          getUserName(subject));

        super.service(request, response, repository);
      } else {
        LOG.info("{} access to repository {} for user {} denied",
          getActionAsString(writeRequest), repository.getName(),
          getUserName(subject));

        sendAccessDenied(request, response);
      }
    } catch (ScmSecurityException | AuthorizationException ex) {
      LOG.warn("user " + subject.getPrincipal() + " has not enough permissions", ex);
      sendAccessDenied(request, response);
    }
  }

  /**
   * Sends a "not enough privileges" error back to client.
   *
   * @param request  http request
   * @param response http response
   * @throws IOException
   */
  protected void sendNotEnoughPrivilegesError(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
    response.sendError(HttpServletResponse.SC_FORBIDDEN);
  }

  /**
   * Sends an unauthorized error back to client.
   *
   * @param request  http request
   * @param response http response
   * @throws IOException
   */
  protected void sendUnauthorizedError(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
    HttpUtil.sendUnauthorized(response, configuration.getRealmDescription());
  }

  /**
   * Send access denied to the servlet response.
   *
   * @param request  current http request object
   * @param response current http response object
   * @throws IOException
   */
  private void sendAccessDenied(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
    if (!Authentications.isAuthenticatedSubjectAnonymous()) {
      sendNotEnoughPrivilegesError(request, response);
    } else {
      sendUnauthorizedError(request, response);
    }
  }

  private String getActionAsString(boolean writeRequest) {
    return writeRequest
      ? "write"
      : "read";
  }

  /**
   * Returns the username from the given subject or anonymous.
   *
   * @param subject user subject
   */
  private Object getUserName(Subject subject) {
    Object principal = subject.getPrincipal();

    if (principal == null) {
      principal = SCMContext.USER_ANONYMOUS;
    }

    return principal;
  }

  /**
   * Returns true if the current user has the required permissions.
   *
   * @param repository   repository for the permissions check
   * @param writeRequest true if request is a write request
   * @return true if the current user has the required permissions
   */
  public static boolean hasPermission(Repository repository, boolean writeRequest) {
    boolean permitted;

    if (writeRequest) {
      permitted = RepositoryPermissions.push(repository).isPermitted();
    } else {
      permitted = RepositoryPermissions.pull(repository).isPermitted();
    }

    return permitted;
  }
}
