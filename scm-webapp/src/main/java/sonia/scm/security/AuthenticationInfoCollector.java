/**
 * Copyright (c) 2014, Sebastian Sdorra
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

import com.google.inject.Inject;
import java.util.Set;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.group.GroupNames;
import sonia.scm.user.User;
import sonia.scm.web.security.AuthenticationResult;

/**
 * Collects authentication info for realm.
 * 
 * @author Sebastian Sdorra
 * @since 1.52
 */
public class AuthenticationInfoCollector {
  
  /**
   * the logger for AuthenticationInfoCollector
   */
  private static final Logger LOG = LoggerFactory.getLogger(AuthenticationInfoCollector.class);
  
  private final LocalDatabaseSynchronizer synchronizer;
  private final GroupCollector groupCollector; 
  private final SessionStore sessionStore;

  /**
   * Construct a new AuthenticationInfoCollector.
   * 
   * @param synchronizer local database synchronizer
   * @param groupCollector groups collector
   * @param sessionStore session store
   */
  @Inject
  public AuthenticationInfoCollector(
    LocalDatabaseSynchronizer synchronizer, GroupCollector groupCollector, SessionStore sessionStore
  ) {
    this.synchronizer = synchronizer;
    this.groupCollector = groupCollector;
    this.sessionStore = sessionStore;
  }
  
  /**
   * Creates authentication info from token and authentication result.
   * 
   * @param token username and password token
   * @param authenticationResult authentication result
   * 
   * @return authentication info
   */
  public AuthenticationInfo createAuthenticationInfo(
    UsernamePasswordToken token, AuthenticationResult authenticationResult
  ) {
    User user = authenticationResult.getUser();
    Set<String> groups = groupCollector.collectGroups(authenticationResult);
    
    synchronizer.synchronize(user, groups);
    
    if (isUserIsDisabled(user)) {
      throwAccountIsDisabledExceptionAndLog(user.getName());
    }
    
    PrincipalCollection collection = createPrincipalCollection(user, groups);
    
    AuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(collection, token.getPassword());
    sessionStore.store(token);
    return authenticationInfo;
  }
  
  private PrincipalCollection createPrincipalCollection(User user, Set<String> groups) {
    SimplePrincipalCollection collection = new SimplePrincipalCollection();
    collection.add(user.getId(), ScmRealm.NAME);
    collection.add(user, ScmRealm.NAME);
    collection.add(new GroupNames(groups), ScmRealm.NAME);
    return collection;
  }
  
  private boolean isUserIsDisabled(User user) {
    return !user.isActive();
  }
  
  private void throwAccountIsDisabledExceptionAndLog(String username) {
    String msg = "user ".concat(username).concat(" is deactivated");
    LOG.warn(msg);
    throw new DisabledAccountException(msg);
  }
}
