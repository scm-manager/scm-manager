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

package sonia.scm.initialization;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.apache.shiro.subject.SimplePrincipalCollection;
import sonia.scm.plugin.Extension;
import sonia.scm.security.AccessToken;
import sonia.scm.security.AccessTokenResolver;
import sonia.scm.security.BearerToken;
import sonia.scm.user.User;

import static com.google.common.base.Preconditions.checkArgument;

@Extension
@Singleton
public class InitializationRealm extends AuthenticatingRealm {

  private static final String REALM = "InitializationRealm";
  public static final String INIT_PRINCIPAL = "__SCM_INIT__";

  private final InitializationAuthenticationService authenticationService;
  private final AccessTokenResolver accessTokenResolver;

  @Inject
  public InitializationRealm(InitializationAuthenticationService authenticationService, AccessTokenResolver accessTokenResolver) {
    this.authenticationService = authenticationService;
    this.accessTokenResolver = accessTokenResolver;
    setAuthenticationTokenClass(InitializationToken.class);
    setCredentialsMatcher(new AllowAllCredentialsMatcher());
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    checkArgument(token instanceof InitializationToken, "%s is required", InitializationToken.class);
    AccessToken accessToken = accessTokenResolver.resolve(BearerToken.valueOf(token.getCredentials().toString()));
    authenticationService.validateToken(accessToken);
    SimplePrincipalCollection principalCollection = new SimplePrincipalCollection(INIT_PRINCIPAL, REALM);
    principalCollection.add(new User(INIT_PRINCIPAL), REALM);
    authenticationService.setPermissions();
    return new SimpleAuthenticationInfo(principalCollection, token.getCredentials());
  }

  @Override
  public boolean supports(AuthenticationToken token) {
    return token instanceof InitializationToken;
  }
}
