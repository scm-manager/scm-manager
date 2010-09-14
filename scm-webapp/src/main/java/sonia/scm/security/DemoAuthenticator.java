/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.security;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.User;

//~--- JDK imports ------------------------------------------------------------

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Sebastian Sdorra
 */
public class DemoAuthenticator implements Authenticator
{

  /** Field description */
  private static final String DEMO_DISPLAYNAME = "Hans am Schalter";

  /** Field description */
  private static final String DEMO_MAIL = "hans@schalter.de";

  /** Field description */
  private static final String DEMO_PASSWORD = "hans123";

  /** Field description */
  private static final String DEMO_USERNAME = "hans";

  //~--- methods --------------------------------------------------------------

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
  @Override
  public User authenticate(HttpServletRequest request, String username,
                           String password)
  {
    User user = null;

    if (DEMO_USERNAME.equals(username) && DEMO_PASSWORD.equals(password))
    {
      user = new User(username, DEMO_DISPLAYNAME, DEMO_MAIL);

      HttpSession session = request.getSession(true);

      session.setAttribute(DemoAuthenticator.class.getName(), user);
    }

    return user;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   */
  @Override 
  public User getUser(HttpServletRequest request)
  {
    User user = null;
    HttpSession session = request.getSession();

    if (session != null)
    {
      user = (User) session.getAttribute(DemoAuthenticator.class.getName());
    }

    return user;
  }
}
