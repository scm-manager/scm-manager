/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.security;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import sonia.scm.web.security.AuthenticationResult;
import sonia.scm.web.security.AuthenticationState;

//~--- JDK imports ------------------------------------------------------------


/**
 * SCM-Manager authentication realm.
 * 
 * @author Sebastian Sdorra
 */
@Singleton
public class ScmRealm extends AuthorizingRealm {
  
  public static final String NAME = "scm";
  

  private final AuthenticatorFacade authenticator;
  private final LoginAttemptHandler loginAttemptHandler;
  private final AuthenticationInfoCollector authcCollector;
  private final AuthorizationCollector authzCollector;
  
  /**
   * Constructs a new scm realm.
   *
   * @param authenticator authenticator facade
   * @param loginAttemptHandler login attempt handler
   * @param authcCollector authentication info collector
   * @param authzCollector authorization collector
   */
  @Inject
  public ScmRealm(AuthenticatorFacade authenticator, LoginAttemptHandler loginAttemptHandler, 
    AuthenticationInfoCollector authcCollector, AuthorizationCollector authzCollector) {
    this.authenticator = authenticator;
    this.loginAttemptHandler = loginAttemptHandler;
    this.authcCollector = authcCollector;
    this.authzCollector = authzCollector;
    

    // set token class
    setAuthenticationTokenClass(UsernamePasswordToken.class);

    // use own custom caching
    setCachingEnabled(false);
    setAuthenticationCachingEnabled(false);
    setAuthorizationCachingEnabled(false);

    // set components
    setPermissionResolver(new RepositoryPermissionResolver());
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authToken) throws AuthenticationException {
    UsernamePasswordToken token = castToken(authToken);

    loginAttemptHandler.beforeAuthentication(token);
    AuthenticationResult result = authenticator.authenticate(token);

    if (isSuccessful(result)) {
      loginAttemptHandler.onSuccessfulAuthentication(token, result);
      return authcCollector.createAuthenticationInfo(token, result);
    } else if (isAccountNotFound(result)) {
      throw new UnknownAccountException("unknown account ".concat(token.getUsername()));
    } else {
      loginAttemptHandler.onUnsuccessfulAuthentication(authToken, result);

      throw new AccountException("authentication failed");
    }
  }
  
  private UsernamePasswordToken castToken(AuthenticationToken token) {
    if (!(token instanceof UsernamePasswordToken)) {
      throw new UnsupportedTokenException("UsernamePasswordToken is required");
    }
    return (UsernamePasswordToken) token;
  }
  
  private boolean isSuccessful(AuthenticationResult result) {
    return result != null && AuthenticationState.SUCCESS == result.getState();
  }
  
  private boolean isAccountNotFound(AuthenticationResult result) {
    return result != null && AuthenticationState.NOT_FOUND == result.getState();
  }

  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals){
    return authzCollector.collect(principals);
  }
}
