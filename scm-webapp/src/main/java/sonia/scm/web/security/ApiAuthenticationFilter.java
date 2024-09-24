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

package sonia.scm.web.security;


import com.google.inject.Inject;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sonia.scm.Priority;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.filter.Filters;
import sonia.scm.filter.WebElement;
import sonia.scm.security.SecurityRequests;
import sonia.scm.web.WebTokenGenerator;
import sonia.scm.web.filter.AuthenticationFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Filter to handle authentication for the rest api of SCM-Manager.
 *
 */
@Priority(Filters.PRIORITY_AUTHENTICATION)
@WebElement(value = Filters.PATTERN_RESTAPI,
  morePatterns = { Filters.PATTERN_DEBUG })
public class ApiAuthenticationFilter extends AuthenticationFilter
{


  /**
   * Constructs a new ApiAuthenticationFilter
   *
   * @param configuration scm main configuration
   * @param tokenGenerators web token generators
   */
  @Inject
  public ApiAuthenticationFilter(ScmConfiguration configuration,
    Set<WebTokenGenerator> tokenGenerators)
  {
    super(configuration, tokenGenerators);
  }


  /**
   * The filter skips the authentication chain on the login resource, for all
   * other resources the request is delegated to the 
   * {@link AuthenticationFilter}.
   *
   * @param request http servlet request
   * @param response http servlet response
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
    // skip filter on login resource
    if (SecurityRequests.isAuthenticationRequest(request) )
    {
      chain.doFilter(request, response);
    }
    else
    {
      super.doFilter(request, response, chain);
    }
  }

  /**
   * The filter process the chain on unauthorized requests and does not prompt 
   * for authentication.
   *
   * @param request http servlet request
   * @param response http servlet response
   * @param chain filter chain
   *
   * @throws IOException
   * @throws ServletException
   */
  @Override
  protected void handleUnauthorized(HttpServletRequest request,
    HttpServletResponse response, FilterChain chain)
    throws IOException, ServletException
  {
    chain.doFilter(request, response);
  }
}
