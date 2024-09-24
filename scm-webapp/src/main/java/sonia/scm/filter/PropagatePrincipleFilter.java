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

package sonia.scm.filter;


import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import sonia.scm.Priority;
import sonia.scm.SCMContext;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.security.AnonymousMode;
import sonia.scm.web.filter.HttpFilter;
import sonia.scm.web.filter.PropagatePrincipleServletRequestWrapper;

import java.io.IOException;


@Priority(Filters.PRIORITY_AUTHORIZATION)
public class PropagatePrincipleFilter extends HttpFilter
{

  /** name of request attribute for the primary principal */
  @VisibleForTesting
  static final String ATTRIBUTE_REMOTE_USER = "principal";

  private final ScmConfiguration configuration;

  @Inject
  public PropagatePrincipleFilter(ScmConfiguration configuration)
  {
    this.configuration = configuration;
  }

  @Override
  protected void doFilter(HttpServletRequest request,
    HttpServletResponse response, FilterChain chain)
    throws IOException, ServletException
  {
    Subject subject = SecurityUtils.getSubject();
    if (hasPermission(subject))
    {
      // add primary principal as request attribute
      // see https://goo.gl/JRjNmf
      String username = getUsername(subject);
      request.setAttribute(ATTRIBUTE_REMOTE_USER, username);

      // wrap servlet request to provide authentication information
      chain.doFilter(new PropagatePrincipleServletRequestWrapper(request, username), response);
    }
    else
    {
      chain.doFilter(request, response);
    }
  }

  private boolean hasPermission(Subject subject)
  {
    return ((configuration != null)
      && configuration.getAnonymousMode() != AnonymousMode.OFF) || subject.isAuthenticated()
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
}
