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



package sonia.scm.legacy;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.crypto.hash.Sha1Hash;
import org.apache.shiro.realm.AuthenticatingRealm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.plugin.Extension;

import sonia.scm.security.DAORealmHelper;
import sonia.scm.security.DAORealmHelperFactory;

//~--- JDK imports ------------------------------------------------------------

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Support for SCM-Manager 1.x password hashes.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
@Extension
@Singleton
public class LegacyRealm extends AuthenticatingRealm
{

  /** Field description */
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
  
  //~--- constructors ---------------------------------------------------------
  
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

    matcher.setHashAlgorithmName(Sha1Hash.ALGORITHM_NAME);
    matcher.setHashIterations(1);
    matcher.setStoredCredentialsHexEncoded(true);
    setCredentialsMatcher(helper.wrapCredentialsMatcher(matcher));
  }

  //~--- methods --------------------------------------------------------------

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
