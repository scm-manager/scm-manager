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
import com.google.inject.Injector;
import com.google.inject.Singleton;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.util.ThreadState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.SCMContext;
import sonia.scm.group.GroupNames;
import sonia.scm.security.Role;
import sonia.scm.user.User;
import sonia.scm.util.AssertUtil;

//~--- JDK imports ------------------------------------------------------------

import java.net.URL;

import javax.xml.bind.JAXB;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class DefaultAdministrationContext implements AdministrationContext
{

  /** Field description */
  public static final String SYSTEM_ACCOUNT =
    "/sonia/scm/web/security/system-account.xml";

  /** Field description */
  private static final String REALM = "AdminRealm";

  /** the logger for DefaultAdministrationContext */
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultAdministrationContext.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param injector
   * @param securityManager
   */
  @Inject
  public DefaultAdministrationContext(Injector injector,
    org.apache.shiro.mgt.SecurityManager securityManager)
  {
    this.injector = injector;
    this.securityManager = securityManager;

    URL url = DefaultAdministrationContext.class.getResource(SYSTEM_ACCOUNT);

    if (url == null)
    {
      throw new RuntimeException("could not find resource for system account");
    }

    User adminUser = JAXB.unmarshal(url, User.class);

    principalCollection = createAdminCollection(adminUser);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param action
   */
  @Override
  public void runAsAdmin(PrivilegedAction action)
  {
    AssertUtil.assertIsNotNull(action);

    if (ThreadContext.getSecurityManager() != null)
    {
      Subject subject = SecurityUtils.getSubject();

      if (subject.hasRole(Role.ADMIN))
      {
        logger.debug(
          "user is already an admin, we need no system account session, execute action {}",
          action.getClass().getName());
        action.run();
      }
      else
      {
        doRunAsInWebSessionContext(action);
      }
    }
    else
    {
      doRunAsInNonWebSessionContext(action);
    }

  }

  /**
   * Method description
   *
   *
   * @param actionClass
   */
  @Override
  public void runAsAdmin(Class<? extends PrivilegedAction> actionClass)
  {
    PrivilegedAction action = injector.getInstance(actionClass);

    runAsAdmin(action);
  }

  /**
   * Method description
   *
   *
   * @param adminUser
   *
   * @return
   */
  private PrincipalCollection createAdminCollection(User adminUser)
  {
    SimplePrincipalCollection collection = new SimplePrincipalCollection();

    collection.add(adminUser.getId(), REALM);
    collection.add(adminUser, REALM);
    collection.add(new GroupNames(), REALM);

    return collection;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private Subject createAdminSubject()
  {
    //J-
    return new Subject.Builder(securityManager)
      .authenticated(true)
      .principals(principalCollection)
      .buildSubject();
    //J+
  }

  /**
   * Method description
   *
   *
   * @param action
   */
  private void doRunAsInNonWebSessionContext(PrivilegedAction action)
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("bind shiro security manager to current thread");
    }

    try
    {
      SecurityUtils.setSecurityManager(securityManager);

      Subject subject = createAdminSubject();
      ThreadState state = new SubjectThreadState(subject);

      state.bind();

      try
      {
        if (logger.isInfoEnabled())
        {
          logger.info("execute action {} in administration context",
            action.getClass().getName());
        }

        action.run();
      }
      finally
      {
        state.clear();
      }
    }
    finally
    {
      SecurityUtils.setSecurityManager(null);
    }
  }

  /**
   * Method description
   *
   *
   * @param action
   */
  private void doRunAsInWebSessionContext(PrivilegedAction action)
  {
    Subject subject = SecurityUtils.getSubject();

    String principal = (String) subject.getPrincipal();

    if (logger.isInfoEnabled())
    {
      String username;

      if (subject.hasRole(Role.USER))
      {
        username = principal;
      }
      else
      {
        username = SCMContext.USER_ANONYMOUS;
      }

      logger.info("user {} executes {} as admin", username,
        action.getClass().getName());
    }

    Subject adminSubject = createAdminSubject();

    // do not use runas, because we want only execute this action in this
    // thread as administrator. Runas could affect other threads

    ThreadContext.bind(adminSubject);

    try
    {
      action.run();
    }
    finally
    {
      logger.debug("release administration context for user {}/{}", principal,
        subject.getPrincipal());
      ThreadContext.bind(subject);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final Injector injector;

  /** Field description */
  private final org.apache.shiro.mgt.SecurityManager securityManager;

  /** Field description */
  private PrincipalCollection principalCollection;
}
