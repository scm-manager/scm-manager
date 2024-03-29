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
