/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.web.security;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.User;

//~--- JDK imports ------------------------------------------------------------

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Sebastian Sdorra
 */
public interface Authenticator
{

  /**
   * Method description
   *
   *
   * @param request
   * @param username
   * @param password
   *
   * @return
   */
  public User authenticate(HttpServletRequest request, String username,
                           String password);

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   */
  public User getUser(HttpServletRequest request);
}
