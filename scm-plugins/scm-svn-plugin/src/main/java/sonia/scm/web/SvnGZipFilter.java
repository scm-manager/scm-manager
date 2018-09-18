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


package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.filter.GZipFilter;
import sonia.scm.repository.Repository;
import sonia.scm.repository.SvnRepositoryHandler;
import sonia.scm.repository.spi.ScmProviderHttpServlet;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class SvnGZipFilter extends GZipFilter implements ScmProviderHttpServlet
{

  private static final Logger logger = LoggerFactory.getLogger(SvnGZipFilter.class);

  private final SvnRepositoryHandler handler;
  private final ScmProviderHttpServlet delegate;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param handler
   */
  public SvnGZipFilter(SvnRepositoryHandler handler, ScmProviderHttpServlet delegate)
  {
    this.handler = handler;
    this.delegate = delegate;
  }

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
    super.init(filterConfig);
    getConfig().setBufferResponse(false);
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
    if (handler.getConfig().isEnabledGZip())
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("encode svn request with gzip");
      }

      super.doFilter(request, response, chain);
    }
    else
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("skip gzip encoding");
      }

      chain.doFilter(request, response);
    }
  }

  @Override
  public void service(HttpServletRequest request, HttpServletResponse response, Repository repository) throws ServletException, IOException {
    if (handler.getConfig().isEnabledGZip())
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("encode svn request with gzip");
      }

      super.doFilter(request, response, (servletRequest, servletResponse) -> delegate.service((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse, repository));
    }
    else
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("skip gzip encoding");
      }

      delegate.service(request, response, repository);
    }
  }

  @Override
  public void init(ServletConfig config) throws ServletException {
    delegate.init(config);
  }
}
