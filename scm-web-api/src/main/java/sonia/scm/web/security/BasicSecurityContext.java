/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.web.security;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.servlet.SessionScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.user.User;
import sonia.scm.user.UserManager;

//~--- JDK imports ------------------------------------------------------------

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
@SessionScoped
public class BasicSecurityContext implements WebSecurityContext
{

  /** the logger for BasicSecurityContext */
  private static final Logger logger =
    LoggerFactory.getLogger(BasicSecurityContext.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param authenticator
   * @param userManager
   */
  @Inject
  public BasicSecurityContext(AuthenticationManager authenticator,
                              UserManager userManager)
  {
    this.authenticator = authenticator;
    this.userManager = userManager;
  }

  //~--- methods --------------------------------------------------------------

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
  @Override
  public User authenticate(HttpServletRequest request,
                           HttpServletResponse response, String username,
                           String password)
  {
    user = authenticator.authenticate(request, response, username, password);

    if (user != null)
    {
      try
      {
        user.setLastLogin(System.currentTimeMillis());

        if (userManager.contains(username))
        {
          userManager.modify(user);
        }
        else
        {
          userManager.create(user);
        }
      }
      catch (Exception ex)
      {
        user = null;
        logger.error(ex.getMessage(), ex);
      }
    }

    return user;
  }

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   */
  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response)
  {
    user = null;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
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
  public boolean isAuthenticated()
  {
    return user != null;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private AuthenticationManager authenticator;

  /** Field description */
  private User user;

  /** Field description */
  private UserManager userManager;
}
