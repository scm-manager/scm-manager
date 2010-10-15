/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.web.filter;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import sonia.scm.User;
import sonia.scm.util.Util;
import sonia.scm.web.security.SecurityContext;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.core.util.Base64;

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
public class BasicAuthenticationFilter extends HttpFilter
{

  /** Field description */
  public static final String AUTHENTICATION_REALM = "SONIA :: SCM Manager";

  /** Field description */
  public static final String AUTHORIZATION_BASIC_PREFIX = "BASIC";

  /** Field description */
  public static final String CREDENTIAL_SEPARATOR = ":";

  /** Field description */
  public static final String HEADERVALUE_CONNECTION_CLOSE = "close";

  /** Field description */
  public static final String HEADER_AUTHORIZATION = "Authorization";

  /** Field description */
  public static final String HEADER_CONNECTION = "connection";

  /** Field description */
  public static final String HEADER_WWW_AUTHENTICATE = "WWW-Authenticate";

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
    User user = null;

    if (securityContext != null)
    {
      if (!securityContext.isAuthenticated())
      {
        String authentication = request.getHeader(HEADER_AUTHORIZATION);

        if (Util.isEmpty(authentication))
        {
          sendUnauthorized(response);
        }
        else
        {
          if (!authentication.toUpperCase().startsWith(
                  AUTHORIZATION_BASIC_PREFIX))
          {
            throw new ServletException("wrong basic header");
          }

          String token = authentication.substring(6);

          token = new String(Base64.decode(token.getBytes()));

          String[] credentials = token.split(CREDENTIAL_SEPARATOR);

          user = securityContext.authenticate(request, response,
                  credentials[0], credentials[1]);
        }
      }
      else
      {
        user = securityContext.getUser();
      }
    }

    if (user != null)
    {
      chain.doFilter(new SecurityHttpServletRequestWrapper(request, user),
                     response);
    }
    else
    {
      sendUnauthorized(response);
    }
  }

  /**
   * Method description
   *
   *
   * @param response
   */
  private void sendUnauthorized(HttpServletResponse response)
  {
    response.setHeader(HEADER_WWW_AUTHENTICATE,
                       "Basic realm=\"" + AUTHENTICATION_REALM + "\"");
    response.setHeader(HEADER_CONNECTION, HEADERVALUE_CONNECTION_CLOSE);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Inject
  private Provider<SecurityContext> securityContextProvider;
}
