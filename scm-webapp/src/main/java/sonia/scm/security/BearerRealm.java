/**
 * Copyright (c) 2014, Sebastian Sdorra All rights reserved.
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



package sonia.scm.security;

import com.google.common.annotations.VisibleForTesting;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.realm.AuthenticatingRealm;
import sonia.scm.group.GroupDAO;
import sonia.scm.plugin.Extension;
import sonia.scm.user.UserDAO;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkArgument;


/**
 * Realm for authentication with {@link BearerToken}.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
@Singleton
@Extension
public class BearerRealm extends AuthenticatingRealm
{
  
  /** realm name */
  @VisibleForTesting
  static final String REALM = "BearerRealm";


  /** dao realm helper */
  private final DAORealmHelper helper;

  /** access token resolver **/
  private final AccessTokenResolver tokenResolver;

  /**
   * Constructs ...
   *
   * @param helperFactory dao realm helper factory
   * @param tokenResolver resolve access token from bearer
   */
  @Inject
  public BearerRealm(DAORealmHelperFactory helperFactory, AccessTokenResolver tokenResolver) {
    this.helper = helperFactory.create(REALM);
    this.tokenResolver = tokenResolver;

    setCredentialsMatcher(new AllowAllCredentialsMatcher());
    setAuthenticationTokenClass(BearerToken.class);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Validates the given bearer token and retrieves authentication data from
   * {@link UserDAO} and {@link GroupDAO}.
   *
   *
   * @param token bearer token
   *
   * @return authentication data from user and group dao
   */
  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
    checkArgument(token instanceof BearerToken, "%s is required", BearerToken.class);

    BearerToken bt = (BearerToken) token;
    AccessToken accessToken = tokenResolver.resolve(bt);

    return helper.authenticationInfoBuilder(accessToken.getSubject())
      .withCredentials(bt.getCredentials())
      .withScope(Scopes.fromClaims(accessToken.getClaims()))
      .build();
  }

}
