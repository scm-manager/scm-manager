/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.filter;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.WebUtil;
import sonia.scm.web.filter.HttpFilter;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
          StringBuilder msg = new StringBuilder("return ");

          msg.append(HttpServletResponse.SC_NOT_MODIFIED);
          msg.append(" for ").append(uri);
          logger.debug(msg.toString());
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
