/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.lifecycle.modules;


import com.google.inject.name.Names;
import jakarta.servlet.ServletContext;
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
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.mgt.SubjectDAO;
import org.apache.shiro.realm.Realm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.ExtensionProcessor;
import sonia.scm.security.DisabledRememberMeManager;
import sonia.scm.security.ScmAtLeastOneSuccessfulStrategy;
import sonia.scm.security.ScmPermissionResolver;


public class ScmSecurityModule extends ShiroWebModule
{

  private static final int ITERATIONS = 8192;

  private static final Logger logger =
    LoggerFactory.getLogger(ScmSecurityModule.class);

  private final ExtensionProcessor extensionProcessor;
 
  public ScmSecurityModule(ServletContext servletContext, ExtensionProcessor extensionProcessor)
  {
    super(servletContext);
    this.extensionProcessor = extensionProcessor;
  }


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
    bind(ModularRealmAuthenticator.class);
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
    bindConstant().annotatedWith(Names.named("shiro.blockTraversal")).to(false);
    bindConstant().annotatedWith(Names.named("shiro.blockSemicolon")).to(false);

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
    hashService.setHashIterations(ITERATIONS);
    passwordService.setHashService(hashService);

    return passwordService;
  }

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
