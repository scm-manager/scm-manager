/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.security;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import sonia.scm.User;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.security.Principal;


import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
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

      if (!uri.startsWith(URL_AUTHENTICATION))
      {
        User user = authenticator.getUser(request);

        if (user != null)
        {
          chain.doFilter(new ScmHttpServletRequest(request, user), res);
        }
        else
        {
          ((HttpServletResponse) res).sendError(
              HttpServletResponse.SC_UNAUTHORIZED);
        }
      }
      else
      {
        chain.doFilter(req, res);
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

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 10/09/08
   * @author         Enter your name here...
   */
  private static class ScmHttpServletRequest extends HttpServletRequestWrapper
  {

    /**
     * Constructs ...
     *
     *
     * @param request
     * @param user
     */
    public ScmHttpServletRequest(HttpServletRequest request, User user)
    {
      super(request);
      this.user = user;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public String getRemoteUser()
    {
      return user.getName();
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public User getUser()
    {
      return user;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public Principal getUserPrincipal()
    {
      return user;
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private User user;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Inject
  private Authenticator authenticator;
}
