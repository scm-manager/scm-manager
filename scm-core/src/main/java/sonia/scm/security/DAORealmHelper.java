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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.group.Group;
import sonia.scm.group.GroupDAO;
import sonia.scm.group.GroupNames;
import sonia.scm.user.User;
import sonia.scm.user.UserDAO;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * The {@link DAORealmHelper} provides a simple way to authenticate against the
 * {@link UserDAO}. The class is used by the default and the legacy realm.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
public final class DAORealmHelper
{

  /**
   * the logger for DAORealmHelper
   */
  private static final Logger LOG = LoggerFactory.getLogger(DAORealmHelper.class);

  private final LoginAttemptHandler loginAttemptHandler;
  
  private final UserDAO userDAO;
  
  private final GroupDAO groupDAO;

  private final String realm;
  
  //~--- constructors ---------------------------------------------------------
  
  /**
   * Constructs a new instance. Consider to use {@link DAORealmHelperFactory} which
   * handles dependency injection.
   *
   * @param loginAttemptHandler login attempt handler for wrapping credentials matcher
   * @param userDAO user dao
   * @param groupDAO group dao
   * @param realm name of realm
   */
  public DAORealmHelper(LoginAttemptHandler loginAttemptHandler, UserDAO userDAO, GroupDAO groupDAO, String realm) {
    this.loginAttemptHandler = loginAttemptHandler;
    this.realm = realm;
    this.userDAO = userDAO;
    this.groupDAO = groupDAO;
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
   * Method description
   *
   *
   * @param token
   *
   * @return
   *
   * @throws AuthenticationException
   */
  public AuthenticationInfo getAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    checkArgument(token instanceof UsernamePasswordToken, "%s is required", UsernamePasswordToken.class);

    UsernamePasswordToken upt = (UsernamePasswordToken) token;
    String principal = upt.getUsername();

    return getAuthenticationInfo(principal, null, null);
  }

  /**
   * Method description
   *
   *
   * @param principal
   * @param credentials
   * @param scope
   *
   * @return
   */
  public AuthenticationInfo getAuthenticationInfo(String principal, String credentials, Scope scope) {
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
    collection.add(collectGroups(principal), realm);
    collection.add(MoreObjects.firstNonNull(scope, Scope.empty()), realm);

    String creds = credentials;

    if (credentials == null) {
      creds = user.getPassword();
    }

    return new SimpleAuthenticationInfo(collection, creds);
  }

  //~--- methods --------------------------------------------------------------

  private GroupNames collectGroups(String principal) {
    Builder<String> builder = ImmutableSet.builder();

    builder.add(GroupNames.AUTHENTICATED);

    for (Group group : groupDAO.getAll()) {
      if (group.isMember(principal)) {
        builder.add(group.getName());
      }
    }

    GroupNames groups = new GroupNames(builder.build());
    LOG.debug("collected following groups for principal {}: {}", principal, groups);
    return groups;
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
