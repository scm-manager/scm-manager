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

package sonia.scm.security;

import com.google.common.annotations.VisibleForTesting;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.group.GroupDAO;
import sonia.scm.plugin.Extension;
import sonia.scm.user.UserDAO;

import static com.google.common.base.Preconditions.checkArgument;


/**
 * Realm for authentication with {@link BearerToken}.
 *
 * @since 2.0.0
 */
@Singleton
@Extension
public class BearerRealm extends AuthenticatingRealm {

  @VisibleForTesting
  static final String REALM = "BearerRealm";

  private static final Logger LOG = LoggerFactory.getLogger(BearerRealm.class);

  private final DAORealmHelper helper;
  private final AccessTokenResolver tokenResolver;


  @Inject
  public BearerRealm(DAORealmHelperFactory helperFactory, AccessTokenResolver tokenResolver) {
    this.helper = helperFactory.create(REALM);
    this.tokenResolver = tokenResolver;

    setCredentialsMatcher(new AllowAllCredentialsMatcher());
    setAuthenticationTokenClass(BearerToken.class);
  }

  @Override
  public boolean supports(AuthenticationToken token) {
    if (token instanceof BearerToken) {
      boolean containsDot = ((BearerToken) token).getCredentials().contains(".");
      if (!containsDot) {
        LOG.debug("Ignoring token without a dot ('.'); this probably is an API key");
      }
      return containsDot;
    }
    return false;
  }

  /**
   * Validates the given bearer token and retrieves authentication data from
   * {@link UserDAO} and {@link GroupDAO}.
   *
   * @param token bearer token
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
      .withSessionId(bt.getPrincipal())
      .build();
  }
}
