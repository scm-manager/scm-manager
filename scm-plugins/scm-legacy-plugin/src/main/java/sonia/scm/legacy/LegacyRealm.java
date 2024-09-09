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

package sonia.scm.legacy;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.Extension;
import sonia.scm.security.DAORealmHelper;
import sonia.scm.security.DAORealmHelperFactory;

/**
 * Support for SCM-Manager 1.x password hashes.
 *
 * @since 2.0.0
 */
@Extension
@Singleton
public class LegacyRealm extends AuthenticatingRealm {

  @VisibleForTesting
  static final String REALM = "LegacyRealm";

  private static final CharMatcher HEX_MATCHER = CharMatcher
    .inRange('0', '9')
    .or(CharMatcher.inRange('a', 'f'))
    .or(CharMatcher.inRange('A', 'F')
    );

 
  private static final Logger LOG = LoggerFactory.getLogger(LegacyRealm.class);

  private final DAORealmHelper helper;

  @Inject
  public LegacyRealm(DAORealmHelperFactory helperFactory) {
    this.helper = helperFactory.create(REALM);
    setAuthenticationTokenClass(UsernamePasswordToken.class);

    HashedCredentialsMatcher matcher = new HashedCredentialsMatcher();

    matcher.setHashAlgorithmName("SHA-1");
    matcher.setHashIterations(1);
    matcher.setStoredCredentialsHexEncoded(true);
    setCredentialsMatcher(helper.wrapCredentialsMatcher(matcher));
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    Preconditions.checkArgument(token instanceof UsernamePasswordToken, "unsupported token");
    return returnOnHexCredentials(helper.getAuthenticationInfo(token));
  }

  private AuthenticationInfo returnOnHexCredentials(AuthenticationInfo info) {
    AuthenticationInfo result = null;

    if (info != null) {
      Object credentials = info.getCredentials();

      if (credentials instanceof String) {
        String password = (String) credentials;

        if (HEX_MATCHER.matchesAllOf(password)) {
          result = info;
        } else {
          LOG.debug("hash contains non hex chars");
        }
      } else {
        LOG.debug("non string crendentials found");
      }
    }
    return result;
  }

}
