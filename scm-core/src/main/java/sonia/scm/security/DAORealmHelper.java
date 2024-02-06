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


import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.user.User;
import sonia.scm.user.UserDAO;

import java.util.Collections;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * The {@link DAORealmHelper} provides a simple way to authenticate against the
 * {@link UserDAO}. The class is used by the default and the legacy realm.
 *
 * @since 2.0.0
 */
public final class DAORealmHelper {

  private static final Logger LOG = LoggerFactory.getLogger(DAORealmHelper.class);

  private final LoginAttemptHandler loginAttemptHandler;

  private final UserDAO userDAO;

  private final String realm;


  /**
   * Constructs a new instance. Consider to use {@link DAORealmHelperFactory} which
   * handles dependency injection.
   *
   * @param loginAttemptHandler login attempt handler for wrapping credentials matcher
   * @param userDAO user dao
   * @param realm name of realm
   */
  public DAORealmHelper(LoginAttemptHandler loginAttemptHandler, UserDAO userDAO, String realm) {
    this.loginAttemptHandler = loginAttemptHandler;
    this.realm = realm;
    this.userDAO = userDAO;
  }


  /**
   * Wraps credentials matcher and applies login attempt policies.
   *
   * @param credentialsMatcher credentials matcher to wrap
   *
   * @return wrapped credentials matcher
   */
  public CredentialsMatcher wrapCredentialsMatcher(CredentialsMatcher credentialsMatcher) {
    return new RetryLimitPasswordMatcher(loginAttemptHandler, credentialsMatcher);
  }

  /**
   * Creates {@link AuthenticationInfo} from a {@link UsernamePasswordToken}. The method accepts
   * {@link AuthenticationInfo} as argument, so that the caller does not need to cast.
   *
   * @param token authentication token, it must be {@link UsernamePasswordToken}
   *
   * @return authentication info
   */
  public AuthenticationInfo getAuthenticationInfo(AuthenticationToken token) {
    checkArgument(token instanceof UsernamePasswordToken, "%s is required", UsernamePasswordToken.class);

    UsernamePasswordToken upt = (UsernamePasswordToken) token;
    String principal = upt.getUsername();

    return getAuthenticationInfo(principal, null, null, null);
  }

  public AuthenticationInfoBuilder authenticationInfoBuilder(String principal) {
    return new AuthenticationInfoBuilder(principal);
  }

  private SimpleAuthenticationInfo getAuthenticationInfo(
    String principal, String credentials, Scope scope, SessionId sessionId
  ) {
    checkArgument(!Strings.isNullOrEmpty(principal), "username is required");

    LOG.debug("try to authenticate {}", principal);

    User user = userDAO.get(principal);
    if (user == null) {
      throw new UnknownAccountException(String.format("unknown account %s", principal));
    }

    if (!user.isActive()) {
      throw new DisabledAccountException(String.format("account %s is disabled", principal));
    }

    SimplePrincipalCollection collection = new SimplePrincipalCollection();

    collection.add(principal, realm);
    collection.add(user, realm);
    collection.add(MoreObjects.firstNonNull(scope, Scope.empty()), realm);

    if (sessionId != null) {
      collection.add(sessionId, realm);
    }

    String creds = credentials;

    if (credentials == null) {
      creds = user.getPassword();
    }

    return new SimpleAuthenticationInfo(collection, creds);
  }


  /**
   * Builder class for {@link AuthenticationInfo}.
   */
  public class AuthenticationInfoBuilder {

    private final String principal;

    private String credentials;
    private Scope scope;
    private SessionId sessionId;

    private AuthenticationInfoBuilder(String principal) {
      this.principal = principal;
    }

    /**
     * With credentials uses the given credentials for the {@link AuthenticationInfo}, this is particularly important
     * for caching purposes.
     */
    public AuthenticationInfoBuilder withCredentials(String credentials) {
      this.credentials = credentials;
      return this;
    }

    /**
     * With the scope object it is possible to limit the access permissions to scm-manager.
     */
    public AuthenticationInfoBuilder withScope(Scope scope) {
      this.scope = scope;
      return this;
    }

    public AuthenticationInfoBuilder withSessionId(SessionId sessionId) {
      this.sessionId = sessionId;
      return this;
    }

    /**
     * Build creates the authentication info from the given information.
     *
     * @return authentication info
     */
    public AuthenticationInfo build() {
      return getAuthenticationInfo(principal, credentials, scope, sessionId);
    }

  }

  private static class RetryLimitPasswordMatcher implements CredentialsMatcher {

    private final LoginAttemptHandler loginAttemptHandler;
    private final CredentialsMatcher credentialsMatcher;

    private RetryLimitPasswordMatcher(LoginAttemptHandler loginAttemptHandler, CredentialsMatcher credentialsMatcher) {
      this.loginAttemptHandler = loginAttemptHandler;
      this.credentialsMatcher = credentialsMatcher;
    }

    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
      loginAttemptHandler.beforeAuthentication(token);
      boolean result = credentialsMatcher.doCredentialsMatch(token, info);
      if ( result ) {
        loginAttemptHandler.onSuccessfulAuthentication(token, info);
      } else {
        loginAttemptHandler.onUnsuccessfulAuthentication(token, info);
      }
      return result;
    }

  }

}
