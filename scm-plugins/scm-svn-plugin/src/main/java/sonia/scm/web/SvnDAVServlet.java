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


@Singleton
public class SvnDAVServlet extends DAVServlet implements ScmProviderHttpServlet
{

  private static final String HEADER_CONTEXTPATH = "X-Forwarded-Ctx";

  private static final long serialVersionUID = -1462257085465785945L;

  
  private static final Logger logger =
    LoggerFactory.getLogger(SvnDAVServlet.class);

  private final SvnCollectionRenderer collectionRenderer;

  private final SvnRepositoryHandler handler;

  private final RepositoryProvider repositoryProvider;

  private final RepositoryRequestListenerUtil repositoryRequestListenerUtil;

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


  
  @Override
  protected DAVConfig getDAVConfig()
  {
    return new SvnDAVConfig(super.getDAVConfig(), handler, collectionRenderer,
      repositoryProvider);
  }




  private static class SvnHttpServletRequestWrapper
    extends HttpServletRequestWrapper
  {
    private final Repository repository;

    public SvnHttpServletRequestWrapper(HttpServletRequest request,
      Repository repository)
    {
      super(request);
      this.repository = repository;
    }

  
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

  }

}
