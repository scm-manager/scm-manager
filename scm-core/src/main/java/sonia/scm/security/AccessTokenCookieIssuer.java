package sonia.scm.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Generates cookies and invalidates access token cookies.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
public interface AccessTokenCookieIssuer {

  /**
   * Creates a cookie for token authentication and attaches it to the response.
   *
   * @param request http servlet request
   * @param response http servlet response
   * @param accessToken access token
   */
  void authenticate(HttpServletRequest request, HttpServletResponse response, AccessToken accessToken);
  /**
   * Invalidates the authentication cookie.
   *
   * @param request http servlet request
   * @param response http servlet response
   */
  void invalidate(HttpServletRequest request, HttpServletResponse response);

}
