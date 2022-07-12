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

package sonia.scm.lifecycle.modules;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.name.Names;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.authc.pam.AuthenticationStrategy;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.authz.permission.PermissionResolver;
import org.apache.shiro.crypto.hash.DefaultHashService;
import org.apache.shiro.guice.web.ShiroWebModule;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.mgt.SubjectDAO;
import org.apache.shiro.realm.Realm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.plugin.ExtensionProcessor;

//~--- JDK imports ------------------------------------------------------------

import javax.servlet.ServletContext;
import org.apache.shiro.mgt.RememberMeManager;
import sonia.scm.security.DisabledRememberMeManager;
import sonia.scm.security.ScmAtLeastOneSuccessfulStrategy;
import sonia.scm.security.ScmPermissionResolver;

/**
 *
 * @author Sebastian Sdorra
 */
public class ScmSecurityModule extends ShiroWebModule
{

  /** Field description */
  private static final int ITERATIONS = 8192;

  /**
   *   the logger for ScmSecurityModule
   */
  private static final Logger logger =
    LoggerFactory.getLogger(ScmSecurityModule.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param servletContext
   * @param extensionProcessor
   */
  public ScmSecurityModule(ServletContext servletContext, ExtensionProcessor extensionProcessor)
  {
    super(servletContext);
    this.extensionProcessor = extensionProcessor;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  @SuppressWarnings("unchecked")
  protected void configureShiroWeb()
  {
    bind(PasswordService.class).toInstance(createPasswordService());

    // expose password service to global injector
    expose(PasswordService.class);

    // disable remember me cookie generation
    bind(RememberMeManager.class).to(DisabledRememberMeManager.class);

    // bind authentication strategy
    bind(Authenticator.class).to(ModularRealmAuthenticator.class);
    bind(AuthenticationStrategy.class).to(ScmAtLeastOneSuccessfulStrategy.class);
    bind(PermissionResolver.class).to(ScmPermissionResolver.class);

    // bind realm
    for (Class<? extends Realm> realm : extensionProcessor.byExtensionPoint(Realm.class))
    {
      logger.info("bind security realm {}", realm);
      bindRealm().to(realm);
    }

    // bind constant
    bindConstant().annotatedWith(Names.named("shiro.loginUrl")).to("/index.html");

    // do not block non ascii character,
    // because this would exclude languages which are non ascii based
    bindConstant().annotatedWith(Names.named("shiro.blockNonAscii")).to(false);

    // disable access to mustache resources
    addFilterChain("/**.mustache", filterConfig(ROLES, "nobody"));

    // disable session
    disableSession();
  }

  private void disableSession() {
    addFilterChain("/**", NO_SESSION_CREATION);
    bindConstant().annotatedWith(Names.named("shiro.sessionStorageEnabled")).to(false);

    DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
    DefaultSessionStorageEvaluator sessionStorageEvaluator = new DefaultSessionStorageEvaluator();
    sessionStorageEvaluator.setSessionStorageEnabled(false);
    subjectDAO.setSessionStorageEvaluator(sessionStorageEvaluator);
    bind(SubjectDAO.class).toInstance(subjectDAO);
  }

  /**
   * Creates a {@link PasswordService} with a smaller size of iteration, because
   * large iterations will slow down subversion.
   *
   * @return instance of {@link PasswordService}
   */
  private PasswordService createPasswordService()
  {
    DefaultPasswordService passwordService = new IdempotentPasswordService();
    DefaultHashService hashService = new DefaultHashService();
    hashService.setGeneratePublicSalt(true);
    hashService.setHashIterations(ITERATIONS);
    passwordService.setHashService(hashService);

    return passwordService;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final ExtensionProcessor extensionProcessor;

  static class IdempotentPasswordService extends DefaultPasswordService {

    private boolean isEncrypted(Object password) {
      return password instanceof String && ((String) password).startsWith("$shiro1$SHA-512$");
    }

    @Override
    public String encryptPassword(Object plaintext) {
      if (isEncrypted(plaintext)) {
        return plaintext.toString();
      }
      return super.encryptPassword(plaintext);
    }
  }
}
