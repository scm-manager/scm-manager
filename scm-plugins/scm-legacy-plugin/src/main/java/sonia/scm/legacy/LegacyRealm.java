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
 * @author Sebastian Sdorra
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

  /**
   * the logger for LegacyRealm
   */
  private static final Logger LOG = LoggerFactory.getLogger(LegacyRealm.class);

  private final DAORealmHelper helper;

  /**
   * Constructs a new instance.
   *
   * @param helperFactory dao realm helper factory
   */
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
