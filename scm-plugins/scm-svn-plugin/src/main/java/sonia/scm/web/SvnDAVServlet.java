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
    
package sonia.scm.web;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.internal.server.dav.DAVConfig;
import org.tmatesoft.svn.core.internal.server.dav.DAVServlet;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryProvider;
import sonia.scm.repository.RepositoryRequestListenerUtil;
import sonia.scm.repository.SvnRepositoryHandler;
import sonia.scm.repository.spi.ScmProviderHttpServlet;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.HttpUtil;

import java.io.IOException;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class SvnDAVServlet extends DAVServlet implements ScmProviderHttpServlet
{

  /** Field description */
  private static final String HEADER_CONTEXTPATH = "X-Forwarded-Ctx";

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
   * @param collectionRenderer
   * @param repositoryProvider
   * @param repositoryRequestListenerUtil
   */
  @Inject
  public SvnDAVServlet(SvnRepositoryHandler handler,
    SvnCollectionRenderer collectionRenderer,
    RepositoryProvider repositoryProvider,
    RepositoryRequestListenerUtil repositoryRequestListenerUtil)
  {
    this.handler = handler;
    this.collectionRenderer = collectionRenderer;
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
  public void service(HttpServletRequest request, HttpServletResponse response, Repository repository)
    throws ServletException, IOException
  {
    if (repositoryRequestListenerUtil.callListeners(request, response,
      repository))
    {
      super.service(new SvnHttpServletRequestWrapper(request,
        repository), response);
    }
    else if (logger.isDebugEnabled())
    {
      logger.debug("request aborted by repository request listener");
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
    return new SvnDAVConfig(super.getDAVConfig(), handler, collectionRenderer,
      repositoryProvider);
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

    public SvnHttpServletRequestWrapper(HttpServletRequest request,
      Repository repository)
    {
      super(request);
      this.repository = repository;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public String getContextPath()
    {
      String header = getHeader(HEADER_CONTEXTPATH);

      if ((header == null) ||!isValidContextPath(header))
      {
        header = super.getContextPath();
      }

      return header;
    }

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

      if (repository != null)
      {
        if (pathInfo.startsWith(HttpUtil.SEPARATOR_PATH))
        {
          pathInfo = pathInfo.substring(1);
        }

        pathInfo = pathInfo.substring(repository.getNamespace().length() + 1 + repository.getName().length());
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

      if (repository != null)
      {
        if (!servletPath.endsWith(HttpUtil.SEPARATOR_PATH))
        {
          servletPath = servletPath.concat(HttpUtil.SEPARATOR_PATH);
        }

        servletPath = servletPath + repository.getNamespace() + "/" + repository.getName();
      }

      return servletPath;
    }

    /**
     * Method description
     *
     *
     * @param ctx
     *
     * @return
     */
    private boolean isValidContextPath(String ctx)
    {
      int length = ctx.length();

      boolean result = (length == 0)
                       || ((length > 1)
                         && ctx.startsWith(HttpUtil.SEPARATOR_PATH));

      if (!result)
      {
        logger.warn(
          "header {} contains a non valid context path, fallback to default",
          HEADER_CONTEXTPATH);
      }

      return result;
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private final Repository repository;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final SvnCollectionRenderer collectionRenderer;

  /** Field description */
  private final SvnRepositoryHandler handler;

  /** Field description */
  private final RepositoryProvider repositoryProvider;

  /** Field description */
  private final RepositoryRequestListenerUtil repositoryRequestListenerUtil;
}
