/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.web.security;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.Initable;
import sonia.scm.User;

//~--- JDK imports ------------------------------------------------------------

import java.io.Closeable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
public interface Authenticator extends Initable, Closeable
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
}
