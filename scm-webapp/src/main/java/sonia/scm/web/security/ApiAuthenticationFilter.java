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



package sonia.scm.web.security;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import sonia.scm.Priority;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.filter.Filters;
import sonia.scm.filter.WebElement;
import sonia.scm.security.SecurityRequests;
import sonia.scm.web.WebTokenGenerator;
import sonia.scm.web.filter.AuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------

/**
 * Filter to handle authentication for the rest api of SCM-Manager.
 *
 * @author Sebastian Sdorra
 */
@Priority(Filters.PRIORITY_AUTHENTICATION)
@WebElement(value = Filters.PATTERN_RESTAPI,
  morePatterns = { Filters.PATTERN_DEBUG })
public class ApiAuthenticationFilter extends AuthenticationFilter
{

  //~--- constructors ---------------------------------------------------------

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

  //~--- methods --------------------------------------------------------------

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
    if (SecurityRequests.isAuthenticationRequest(request))
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
