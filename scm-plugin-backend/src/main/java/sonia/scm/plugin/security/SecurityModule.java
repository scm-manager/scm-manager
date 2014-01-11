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


package sonia.scm.plugin.security;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.name.Named;
import com.google.inject.name.Names;

import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.guice.web.ShiroWebModule;
import org.apache.shiro.util.ByteSource;

import sonia.scm.plugin.Roles;

//~--- JDK imports ------------------------------------------------------------

import javax.servlet.ServletContext;

import javax.swing.JOptionPane;

/**
 *
 * @author Sebastian Sdorra
 */
public class SecurityModule extends ShiroWebModule
{

  /** Field description */
  private static final String ATTRIBUTE_FAILURE = "shiroLoginFailure";

  /** Field description */
  private static final String HASH_ALGORITHM = "SHA-256";

  /** Field description */
  private static final int HASH_ITERATIONS = 1024;

  /** Field description */
  private static final String PAGE_LOGIN = "/page/login.html";

  /** Field description */
  private static final String PAGE_SUCCESS = "/admin/index.html";

  /** Field description */
  private static final String PAGE_UNAUTHORIZED = "/error/unauthorized.html";

  /** Field description */
  private static final String PARAM_PASSWORD = "password";

  /** Field description */
  private static final String PARAM_REMEMBERME = "rememberme";

  /** Field description */
  private static final String PARAM_USERNAME = "username";

  /** Field description */
  private static final String PATTERN_ADMIN = "/admin/**";

  /** Field description */
  private static final Named NAMED_USERNAMEPARAM =
    Names.named("shiro.usernameParam");

  /** Field description */
  private static final Named NAMED_UNAUTHORIZEDURL =
    Names.named("shiro.unauthorizedUrl");

  /** Field description */
  private static final Named NAMED_SUCCESSURL = Names.named("shiro.successUrl");

  /** Field description */
  private static final Named NAMED_REMEMBERMEPARAM =
    Names.named("shiro.rememberMeParam");

  /** Field description */
  private static final Named NAMED_PASSWORDPARAM =
    Names.named("shiro.passwordParam");

  /** Field description */
  private static final Named NAMED_LOGINURL = Names.named("shiro.loginUrl");

  /** Field description */
  private static final Named NAMED_FAILUREKEYATTRIBUTE =
    Names.named("shiro.failureKeyAttribute");

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param servletContext
   */
  public SecurityModule(ServletContext servletContext)
  {
    super(servletContext);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param args
   */
  public static void main(String[] args)
  {
    String value = JOptionPane.showInputDialog("Password");
    RandomNumberGenerator rng = new SecureRandomNumberGenerator();
    ByteSource salt = rng.nextBytes();
    SimpleHash hash = new SimpleHash(HASH_ALGORITHM, value, salt,
                        HASH_ITERATIONS);

    System.out.append("Salt: ").println(salt.toBase64());
    System.out.append("Hash: ").println(hash.toBase64());
  }

  /**
   * Method description
   *
   */
  @Override
  @SuppressWarnings("unchecked")
  protected void configureShiroWeb()
  {
    bindConstants();
    bindCredentialsMatcher();

    // bind cache manager
    bind(CacheManager.class).toProvider(CacheManagerProvider.class);

    // bind realm
    bindRealm().to(DefaultAdminRealm.class);

    // add filters
    addFilterChain(PAGE_LOGIN, AUTHC);
    addFilterChain(PATTERN_ADMIN, AUTHC, config(ROLES, Roles.ADMIN));
  }

  /**
   * Method description
   *
   */
  private void bindConstants()
  {
    bindConstant().annotatedWith(NAMED_LOGINURL).to(PAGE_LOGIN);
    bindConstant().annotatedWith(NAMED_USERNAMEPARAM).to(PARAM_USERNAME);
    bindConstant().annotatedWith(NAMED_PASSWORDPARAM).to(PARAM_PASSWORD);
    bindConstant().annotatedWith(NAMED_REMEMBERMEPARAM).to(PARAM_REMEMBERME);
    bindConstant().annotatedWith(NAMED_SUCCESSURL).to(PAGE_SUCCESS);
    bindConstant().annotatedWith(NAMED_UNAUTHORIZEDURL).to(PAGE_UNAUTHORIZED);
    bindConstant().annotatedWith(NAMED_FAILUREKEYATTRIBUTE).to(
      ATTRIBUTE_FAILURE);
  }

  /**
   * Method description
   *
   */
  private void bindCredentialsMatcher()
  {
    HashedCredentialsMatcher matcher =
      new HashedCredentialsMatcher(HASH_ALGORITHM);

    matcher.setHashIterations(HASH_ITERATIONS);
    matcher.setStoredCredentialsHexEncoded(false);
    bind(CredentialsMatcher.class).toInstance(matcher);
  }
}
