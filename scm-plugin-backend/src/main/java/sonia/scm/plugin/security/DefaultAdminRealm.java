/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.plugin.security;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.plugin.AdminAccountConfiguration;
import sonia.scm.plugin.BackendConfiguration;
import sonia.scm.plugin.Roles;

/**
 *
 * @author Sebastian Sdorra
 */
public class DefaultAdminRealm extends AuthorizingRealm
{

  /** Field description */
  public static final String NAME = "scm.backend";

  /**
   * the logger for ScmBackendRealm
   */
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultAdminRealm.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param configuration
   * @param credentialsMatcher
   */
  @Inject
  public DefaultAdminRealm(BackendConfiguration configuration,
    CredentialsMatcher credentialsMatcher)
  {
    super(credentialsMatcher);
    this.configuration = configuration;
    setAuthenticationTokenClass(UsernamePasswordToken.class);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param token
   *
   * @return
   *
   * @throws AuthenticationException
   */
  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(
    AuthenticationToken token)
    throws AuthenticationException
  {
    Preconditions.checkNotNull(token);

    UsernamePasswordToken upToken = (UsernamePasswordToken) token;

    String username = upToken.getUsername();

    if (logger.isDebugEnabled())
    {
      logger.debug("start authentication for user {}", username);
    }

    AdminAccountConfiguration adminAccount = configuration.getAdminAccount();

    if (!adminAccount.getUsername().equals(adminAccount.getUsername()))
    {
      throw new UnknownAccountException("unknown account ".concat(username));
    }

    return adminAccount;
  }

  /**
   * Method description
   *
   *
   * @param principals
   *
   * @return
   */
  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(
    PrincipalCollection principals)
  {
    if (principals == null)
    {
      throw new AuthenticationException("principals should not be null");
    }

    return new SimpleAuthorizationInfo(ImmutableSet.of(Roles.ADMIN));
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private BackendConfiguration configuration;
}
