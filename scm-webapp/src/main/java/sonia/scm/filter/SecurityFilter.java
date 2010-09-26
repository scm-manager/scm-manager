/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.filter;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Singleton;

import sonia.scm.User;
import sonia.scm.web.filter.HttpFilter;
import sonia.scm.web.filter.SecurityHttpServletRequestWrapper;
import sonia.scm.web.security.Authenticator;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.security.Principal;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class SecurityFilter extends HttpFilter
{

  /** Field description */
  public static final String URL_AUTHENTICATION = "/api/rest/authentication";

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
    String uri =
      request.getRequestURI().substring(request.getContextPath().length());

    if (!uri.startsWith(URL_AUTHENTICATION))
    {
      User user = authenticator.getUser(request);

      if (user != null)
      {
        chain.doFilter(new SecurityHttpServletRequestWrapper(request, user),
                       response);
      }
      else
      {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      }
    }
    else
    {
      chain.doFilter(request, response);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Inject
  private Authenticator authenticator;
}
