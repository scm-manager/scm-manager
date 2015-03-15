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



package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.name.Names;

import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.crypto.hash.DefaultHashService;
import org.apache.shiro.guice.web.ShiroWebModule;
import org.apache.shiro.realm.Realm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.plugin.ExtensionProcessor;

import static org.apache.shiro.guice.web.ShiroWebModule.ROLES;

//~--- JDK imports ------------------------------------------------------------

import javax.servlet.ServletContext;

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
  ScmSecurityModule(ServletContext servletContext,
    ExtensionProcessor extensionProcessor)
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

    // bind realm
    for (Class<? extends Realm> realm : extensionProcessor.byExtensionPoint(Realm.class))
    {
      logger.info("bind security realm {}", realm);
      bindRealm().to(realm);
    }

    // bind constant
    bindConstant().annotatedWith(Names.named("shiro.loginUrl")).to(
      "/index.html");

    // disable access to mustache resources
    addFilterChain("/**.mustache", config(ROLES, "nobody"));
    
    // disable session
    // addFilterChain("/**", NO_SESSION_CREATION);
  }

  /**
   * Creates a {@link PasswordService} with a smaller size of iteration, because
   * large iterations will slow down subversion.
   *
   * @return instance of {@link PasswordService}
   */
  private PasswordService createPasswordService()
  {
    DefaultPasswordService passwordService = new DefaultPasswordService();
    DefaultHashService hashService = new DefaultHashService();

    hashService.setHashIterations(ITERATIONS);
    passwordService.setHashService(hashService);

    return passwordService;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final ExtensionProcessor extensionProcessor;
}
