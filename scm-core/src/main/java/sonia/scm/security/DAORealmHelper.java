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

//~--- non-JDK imports --------------------------------------------------------

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
import sonia.scm.group.GroupDAO;
import sonia.scm.user.User;
import sonia.scm.user.UserDAO;

import java.util.Collections;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * The {@link DAORealmHelper} provides a simple way to authenticate against the
 * {@link UserDAO}. The class is used by the default and the legacy realm.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
public final class DAORealmHelper {

  /**
   * the logger for DAORealmHelper
   */
  private static final Logger LOG = LoggerFactory.getLogger(DAORealmHelper.class);

  private final LoginAttemptHandler loginAttemptHandler;
  
  private final UserDAO userDAO;
  
  private final GroupCollector groupCollector;

  private final String realm;
  
  //~--- constructors ---------------------------------------------------------
  
  /**
   * Constructs a new instance. Consider to use {@link DAORealmHelperFactory} which
   * handles dependency injection.
   *
   * @param loginAttemptHandler login attempt handler for wrapping credentials matcher
   * @param userDAO user dao
   * @param groupCollector collect groups for a principal
   * @param realm name of realm
   */
  public DAORealmHelper(LoginAttemptHandler loginAttemptHandler, UserDAO userDAO, GroupCollector groupCollector, String realm) {
    this.loginAttemptHandler = loginAttemptHandler;
    this.realm = realm;
    this.userDAO = userDAO;
    this.groupCollector = groupCollector;
  }

  //~--- get methods ----------------------------------------------------------

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

    return getAuthenticationInfo(principal, null, null, Collections.emptySet());
  }

  /**
   * Returns a builder for {@link AuthenticationInfo}.
   *
   * @param principal name of principal (username)
   *
   * @return authentication info builder
   */
  public AuthenticationInfoBuilder authenticationInfoBuilder(String principal) {
    return new AuthenticationInfoBuilder(principal);
  }


  private AuthenticationInfo getAuthenticationInfo(String principal, String credentials, Scope scope, Iterable<String> groups) {
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
    collection.add(groupCollector.collect(principal, groups), realm);
    collection.add(MoreObjects.firstNonNull(scope, Scope.empty()), realm);

    String creds = credentials;

    if (credentials == null) {
      creds = user.getPassword();
    }

    return new SimpleAuthenticationInfo(collection, creds);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Builder class for {@link AuthenticationInfo}.
   */
  public class AuthenticationInfoBuilder {

    private final String principal;

    private String credentials;
    private Scope scope;
    private Iterable<String> groups = Collections.emptySet();

    private AuthenticationInfoBuilder(String principal) {
      this.principal = principal;
    }

    /**
     * With credentials uses the given credentials for the {@link AuthenticationInfo}, this is particularly important
     * for caching purposes.
     *
     * @param credentials credentials such as password
     *
     * @return {@code this}
     */
    public AuthenticationInfoBuilder withCredentials(String credentials) {
      this.credentials = credentials;
      return this;
    }

    /**
     * With the scope object it is possible to limit the access permissions to scm-manager.
     *
     * @param scope scope object
     *
     * @return {@code this}
     */
    public AuthenticationInfoBuilder withScope(Scope scope) {
      this.scope = scope;
      return this;
    }

    /**
     * With groups adds extra groups, besides those which come from the {@link GroupDAO}, to the authentication info.
     *
     * @param groups extra groups
     *
     * @return {@code this}
     */
    public AuthenticationInfoBuilder withGroups(Iterable<String> groups) {
      this.groups = groups;
      return this;
    }

    /**
     * Build creates the authentication info from the given information.
     *
     * @return authentication info
     */
    public AuthenticationInfo build() {
      return getAuthenticationInfo(principal, credentials, scope, groups);
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
