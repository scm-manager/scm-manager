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
