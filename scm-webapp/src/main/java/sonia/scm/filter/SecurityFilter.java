/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.filter;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import sonia.scm.web.filter.HttpFilter;
import sonia.scm.web.filter.SecurityHttpServletRequestWrapper;
import sonia.scm.web.security.SecurityContext;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
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

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param securityContextProvider
   */
  @Inject
  public SecurityFilter(Provider<SecurityContext> securityContextProvider)
  {
    this.securityContextProvider = securityContextProvider;
  }

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
    SecurityContext securityContext = securityContextProvider.get();

    if (securityContext != null)
    {
      String uri =
        request.getRequestURI().substring(request.getContextPath().length());

      if (!uri.startsWith(URL_AUTHENTICATION))
      {
        if (securityContext.isAuthenticated())
        {
          chain.doFilter(new SecurityHttpServletRequestWrapper(request,
                  securityContext.getUser()), response);
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
    else
    {
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Provider<SecurityContext> securityContextProvider;
}
