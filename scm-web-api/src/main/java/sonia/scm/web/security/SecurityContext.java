/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.web.security;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.User;

//~--- JDK imports ------------------------------------------------------------

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
public interface SecurityContext
{

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   * @param username
   * @param password
   *
   * @return
   */
  public User authenticate(HttpServletRequest request,
                              HttpServletResponse response, String username,
                              String password);

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public User getUser();

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isAuthenticated();
}
