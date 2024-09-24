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
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.MDC;
import sonia.scm.Priority;
import sonia.scm.SCMContext;
import sonia.scm.TransactionId;
import sonia.scm.security.DefaultKeyGenerator;
import sonia.scm.web.filter.HttpFilter;

import java.io.IOException;


@Priority(Filters.PRIORITY_POST_AUTHENTICATION)
@WebElement(Filters.PATTERN_ALL)
public class MDCFilter extends HttpFilter
{
  private static final DefaultKeyGenerator TRANSACTION_KEY_GENERATOR = new DefaultKeyGenerator();

  @VisibleForTesting
  static final String MDC_CLIENT_HOST = "client_host";

  @VisibleForTesting
  static final String MDC_CLIENT_IP = "client_ip";

  @VisibleForTesting
  static final String MDC_REQUEST_URI = "request_uri";

  @VisibleForTesting
  static final String MDC_REQUEST_METHOD = "request_method";

  @VisibleForTesting
  static final String MDC_USERNAME = "username";



  @Override
  protected void doFilter(HttpServletRequest request,
    HttpServletResponse response, FilterChain chain)
    throws IOException, ServletException
  {
    MDC.put(MDC_USERNAME, getUsername());
    MDC.put(MDC_CLIENT_IP, request.getRemoteAddr());
    MDC.put(MDC_CLIENT_HOST, request.getRemoteHost());
    MDC.put(MDC_REQUEST_METHOD, request.getMethod());
    MDC.put(MDC_REQUEST_URI, request.getRequestURI());
    TransactionId.set(TRANSACTION_KEY_GENERATOR.createKey());

    try
    {
      chain.doFilter(request, response);
    }
    finally
    {
      MDC.remove(MDC_USERNAME);
      MDC.remove(MDC_CLIENT_IP);
      MDC.remove(MDC_CLIENT_HOST);
      MDC.remove(MDC_REQUEST_METHOD);
      MDC.remove(MDC_REQUEST_URI);
      TransactionId.clear();
    }
  }



  private String getUsername()
  {
    Subject subject = SecurityUtils.getSubject();
    String username;
    Object principal = subject.getPrincipal();

    if (principal == null)
    {
      username = SCMContext.USER_ANONYMOUS;
    }
    else
    {
      username = principal.toString();
    }

    return username;
  }
}
