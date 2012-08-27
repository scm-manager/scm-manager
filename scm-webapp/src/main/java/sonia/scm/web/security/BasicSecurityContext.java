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

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.config.ScmConfiguration;
import sonia.scm.security.Groups;
import sonia.scm.security.ScmAuthenticationToken;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Sebastian Sdorra
 */
public class BasicSecurityContext implements WebSecurityContext
{

  /** Field description */
  public static final String SCM_CREDENTIALS = "SCM_CREDENTIALS";

  /** Field description */
  public static final String USER_ANONYMOUS = "anonymous";

  /** the logger for BasicSecurityContext */
  private static final Logger logger =
    LoggerFactory.getLogger(BasicSecurityContext.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param configuration
   * @param userManager
   */
  @Inject
  public BasicSecurityContext(ScmConfiguration configuration,
    UserManager userManager)
  {
    this.configuration = configuration;
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
    HttpServletResponse response, String username, String password)
  {
    Subject subject = SecurityUtils.getSubject();

    subject.login(new ScmAuthenticationToken(request, response, username,
      password));

    return subject.getPrincipals().oneByType(User.class);
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
    SecurityUtils.getSubject().logout();

    HttpSession session = request.getSession(false);

    if (session != null)
    {
      session.invalidate();
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<String> getGroups()
  {
    Subject subject = SecurityUtils.getSubject();
    Groups groups = getPrincipal(Groups.class);

    Collection<String> groupCollection = null;

    if (groups != null)
    {
      groupCollection = groups.getGroups();
    }
    else
    {
      groupCollection = Collections.EMPTY_LIST;
    }

    return groupCollection;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public User getUser()
  {
    User user = getPrincipal(User.class);

    if ((user == null) && configuration.isAnonymousAccessEnabled())
    {
      user = userManager.get(USER_ANONYMOUS);
    }

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
    return getUser() != null;
  }

  /**
   * Method description
   *
   *
   * @param clazz
   * @param <T>
   *
   * @return
   */
  private <T> T getPrincipal(Class<T> clazz)
  {
    T result = null;
    Subject subject = SecurityUtils.getSubject();

    if (subject.isAuthenticated())
    {
      PrincipalCollection pc = subject.getPrincipals();

      if (pc != null)
      {
        result = pc.oneByType(clazz);
      }
    }

    return result;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private ScmConfiguration configuration;

  /** Field description */
  private UserManager userManager;
}
