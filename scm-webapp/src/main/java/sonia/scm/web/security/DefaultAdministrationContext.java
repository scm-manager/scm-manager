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
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  /** the logger for DefaultAdministrationContext */
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultAdministrationContext.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param injector
   * @param userSessionProvider
   * @param contextHolder
   */
  @Inject
  public DefaultAdministrationContext(Injector injector,
          @Named("userSession") Provider<WebSecurityContext> userSessionProvider,
          LocalSecurityContextHolder contextHolder)
  {
    this.injector = injector;
    this.userSessionProvider = userSessionProvider;
    this.contextHolder = contextHolder;

    URL url = DefaultAdministrationContext.class.getResource(SYSTEM_ACCOUNT);

    if (url == null)
    {
      throw new RuntimeException("could not find resource for system account");
    }

    User user = JAXB.unmarshal(url, User.class);

    adminContext = new AdministrationSecurityContext(user);
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

    if (logger.isWarnEnabled())
    {
      String user = SecurityUtil.getUsername(userSessionProvider);

      logger.warn("user {} executes {} as admin", user,
                  action.getClass().getName());
    }

    contextHolder.set(adminContext);

    try
    {
      action.run();
    }
    finally
    {
      contextHolder.remove();
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

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private AdministrationSecurityContext adminContext;

  /** Field description */
  private LocalSecurityContextHolder contextHolder;

  /** Field description */
  private Injector injector;

  /** Field description */
  private Provider<WebSecurityContext> userSessionProvider;
}
