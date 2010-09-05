/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.api.rest;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
@WebFilter(urlPatterns = "/api/rest/*")
public class SecurityFilter implements Filter
{

  /** Field description */
  public static final String URL_AUTHENTICATION = "/api/rest/authentication";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  public void destroy()
  {

    // do nothing
  }

  /**
   * Method description
   *
   *
   * @param req
   * @param res
   * @param chain
   *
   * @throws IOException
   * @throws ServletException
   */
  @Override
  public void doFilter(ServletRequest req, ServletResponse res,
                       FilterChain chain)
          throws IOException, ServletException
  {
    if ((req instanceof HttpServletRequest)
        && (res instanceof HttpServletResponse))
    {
      HttpServletRequest request = (HttpServletRequest) req;
      String uri =
        request.getRequestURI().substring(request.getContextPath().length());

      System.out.println( uri + "" + uri.startsWith( URL_AUTHENTICATION ) );

      if (uri.startsWith(URL_AUTHENTICATION)
          || (request.getSession(true).getAttribute("auth") != null))
      {
        chain.doFilter(req, res);
      }
      else
      {
        ((HttpServletResponse) res).sendError(
            HttpServletResponse.SC_UNAUTHORIZED);
      }
    }
    else
    {
      throw new ServletException("request is not an HttpServletRequest");
    }
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

    // do nothing
  }
}
