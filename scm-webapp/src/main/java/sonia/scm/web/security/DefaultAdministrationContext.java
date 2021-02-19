/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
import sonia.scm.security.Authentications;
import sonia.scm.security.Role;
import sonia.scm.user.User;
import sonia.scm.util.AssertUtil;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class DefaultAdministrationContext implements AdministrationContext
{

  /** Field description */
  private static final User SYSTEM_ACCOUNT = new User(
    Authentications.PRINCIPAL_SYSTEM,
    "SCM-Manager System Account",
    null
  );



  /** Field description */
  static final String REALM = "AdminRealm";

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

    principalCollection = createAdminCollection(SYSTEM_ACCOUNT);
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
      doRunAsInWebSessionContext(action);
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
    collection.add(AdministrationContextMarker.MARKER, REALM);

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

  private void doRunAsInNonWebSessionContext(PrivilegedAction action) {
    logger.trace("bind shiro security manager to current thread");

    try {
      SecurityUtils.setSecurityManager(securityManager);

      Subject subject = createAdminSubject();
      ThreadState state = new SubjectThreadState(subject);

      state.bind();
      try
      {
        logger.info("execute action {} in administration context", action.getClass().getName());

        action.run();
      } finally {
        logger.trace("restore current thread state");
        state.restore();
      }
    } finally {
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
