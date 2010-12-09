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



package sonia.scm.boot;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
public class BootstrapFilter implements Filter
{

  /** Field description */
  public static final String FILTER = "com.google.inject.servlet.GuiceFilter";

  /** the logger for BootstrapFilter */
  private static final Logger logger =
    LoggerFactory.getLogger(BootstrapFilter.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  public void destroy()
  {
    if (classLoader != null)
    {
      Thread.currentThread().setContextClassLoader(classLoader);
    }

    guiceFilter.destroy();
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
  public void doFilter(ServletRequest request, ServletResponse response,
                       FilterChain chain)
          throws IOException, ServletException
  {
    if (classLoader != null)
    {
      Thread.currentThread().setContextClassLoader(classLoader);
    }

    guiceFilter.doFilter(request, response, chain);
  }

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
    classLoader =
      BootstrapUtil.getClassLoader(filterConfig.getServletContext());

    if (classLoader != null)
    {
      logger.info("loading GuiceFilter with ScmBootstrapClassLoader");
      Thread.currentThread().setContextClassLoader(classLoader);
      guiceFilter = BootstrapUtil.loadClass(classLoader, Filter.class, FILTER);
    }

    if (guiceFilter == null)
    {
      logger.info("fallback to default classloader for GuiceFilter");
      guiceFilter = BootstrapUtil.loadClass(Filter.class, FILTER);
    }

    guiceFilter.init(filterConfig);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private ClassLoader classLoader;

  /** Field description */
  private Filter guiceFilter;
}
