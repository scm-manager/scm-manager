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
    
package sonia.scm.web.security;

//~--- non-JDK imports --------------------------------------------------------

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
