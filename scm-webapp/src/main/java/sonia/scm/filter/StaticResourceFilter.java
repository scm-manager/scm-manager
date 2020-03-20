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

import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.util.WebUtil;
import sonia.scm.web.filter.HttpFilter;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class StaticResourceFilter extends HttpFilter
{

  /** Field description */
  private static final Logger logger =
    LoggerFactory.getLogger(StaticResourceFilter.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param filterConfig
   *
   * @throws ServletException
   */
  @Override
  public void init(FilterConfig filterConfig) throws ServletException
  {
    this.context = filterConfig.getServletContext();
  }

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
    String uri = request.getRequestURI();
    File resource = getResourceFile(request, uri);

    if (!resource.exists())
    {
      WebUtil.addETagHeader(response, resource);
      WebUtil.addStaticCacheControls(response, WebUtil.TIME_YEAR);

      if (!WebUtil.isModified(request, resource))
      {
        if (logger.isDebugEnabled())
        {
          logger.debug("return {} for {}" , HttpServletResponse.SC_NOT_MODIFIED, uri);
        }

        response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
      }
      else
      {
        chain.doFilter(request, response);
      }
    }
    else
    {
      chain.doFilter(request, response);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   * @param uri
   *
   * @return
   */
  private File getResourceFile(HttpServletRequest request, String uri)
  {
    String path = uri.substring(request.getContextPath().length());

    return new File(context.getRealPath(path));
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private ServletContext context;
}
