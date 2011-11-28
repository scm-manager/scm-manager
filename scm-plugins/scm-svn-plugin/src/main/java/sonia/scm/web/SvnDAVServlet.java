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

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.tmatesoft.svn.core.internal.server.dav.DAVConfig;
import org.tmatesoft.svn.core.internal.server.dav.DAVServlet;

import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryRequestListenerUtil;
import sonia.scm.repository.SvnRepositoryHandler;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.HttpUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class SvnDAVServlet extends DAVServlet
{

  /** Field description */
  private static final long serialVersionUID = -1462257085465785945L;

  /** the logger for SvnDAVServlet */
  private static final Logger logger =
    LoggerFactory.getLogger(SvnDAVServlet.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param handler
   * @param repositoryProvider
   * @param repositoryRequestListenerUtil
   */
  @Inject
  public SvnDAVServlet(
          SvnRepositoryHandler handler,
          Provider<Repository> repositoryProvider,
          RepositoryRequestListenerUtil repositoryRequestListenerUtil)
  {
    this.handler = handler;
    this.repositoryProvider = repositoryProvider;
    this.repositoryRequestListenerUtil = repositoryRequestListenerUtil;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   *
   * @throws IOException
   * @throws ServletException
   */
  @Override
  public void service(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException
  {
    Repository repository = repositoryProvider.get();

    if (repository != null)
    {
      if (repositoryRequestListenerUtil.callListeners(request, response,
              repository))
      {
        super.service(new SvnHttpServletRequestWrapper(request,
                repositoryProvider), response);
      }
      else if (logger.isDebugEnabled())
      {
        logger.debug("request aborted by repository request listener");
      }
    }
    else
    {
      super.service(new SvnHttpServletRequestWrapper(request,
              repositoryProvider), response);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected DAVConfig getDAVConfig()
  {
    return new SvnDAVConfig(super.getDAVConfig(), handler, repositoryProvider);
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 11/10/23
   * @author         Enter your name here...
   */
  private static class SvnHttpServletRequestWrapper
          extends HttpServletRequestWrapper
  {

    /**
     * Constructs ...
     *
     *
     * @param request
     * @param repositoryProvider
     */
    public SvnHttpServletRequestWrapper(HttpServletRequest request,
            Provider<Repository> repositoryProvider)
    {
      super(request);
      this.repositoryProvider = repositoryProvider;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public String getPathInfo()
    {
      String pathInfo = super.getPathInfo();

      AssertUtil.assertIsNotEmpty(pathInfo);

      Repository repository = repositoryProvider.get();

      if (repository != null)
      {
        if (pathInfo.startsWith(HttpUtil.SEPARATOR_PATH))
        {
          pathInfo = pathInfo.substring(1);
        }

        pathInfo = pathInfo.substring(repository.getName().length());
      }

      return pathInfo;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public String getServletPath()
    {
      String servletPath = super.getServletPath();
      Repository repository = repositoryProvider.get();

      if (repository != null)
      {
        if (!servletPath.endsWith(HttpUtil.SEPARATOR_PATH))
        {
          servletPath = servletPath.concat(HttpUtil.SEPARATOR_PATH);
        }

        servletPath = servletPath.concat(repository.getName());
      }

      return servletPath;
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private Provider<Repository> repositoryProvider;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private SvnRepositoryHandler handler;

  /** Field description */
  private Provider<Repository> repositoryProvider;

  /** Field description */
  private RepositoryRequestListenerUtil repositoryRequestListenerUtil;
}
