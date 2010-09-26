/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.web.filter;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.User;

//~--- JDK imports ------------------------------------------------------------

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 *
 * @author Sebastian Sdorra
 */
public class SecurityHttpServletRequestWrapper extends HttpServletRequestWrapper
{

  /**
   * Constructs ...
   *
   *
   * @param request
   * @param user
   */
  public SecurityHttpServletRequestWrapper(HttpServletRequest request,
          User user)
  {
    super(request);
    this.user = user;
  }

  //~--- get methods ----------------------------------------------------------

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

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private User user;
}
