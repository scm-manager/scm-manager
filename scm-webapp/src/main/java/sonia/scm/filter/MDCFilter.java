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

//~--- non-JDK imports --------------------------------------------------------

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

/**
 *
 * @author Sebastian Sdorra
 */
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

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
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
