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

import com.google.common.annotations.VisibleForTesting;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.PasswordMatcher;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import sonia.scm.group.GroupNames;
import sonia.scm.plugin.Extension;

//~--- JDK imports ------------------------------------------------------------

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default authorizing realm.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
@Extension
@Singleton
public class DefaultRealm extends AuthorizingRealm
{
  
  private static final String SEPARATOR = System.getProperty("line.separator", "\n");
  
  /**
   * the logger for DefaultRealm
   */
  private static final Logger LOG = LoggerFactory.getLogger(DefaultRealm.class);

  /** Field description */
  @VisibleForTesting
  static final String REALM = "DefaultRealm";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param service
   * @param collector
   * @param helperFactory
   */
  @Inject
  public DefaultRealm(PasswordService service,
    DefaultAuthorizationCollector collector, DAORealmHelperFactory helperFactory)
  {
    this.collector = collector;
    this.helper = helperFactory.create(REALM);

    PasswordMatcher matcher = new PasswordMatcher();

    matcher.setPasswordService(service);
    setCredentialsMatcher(helper.wrapCredentialsMatcher(matcher));
    setAuthenticationTokenClass(UsernamePasswordToken.class);
  }

  //~--- methods --------------------------------------------------------------

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
  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(
    AuthenticationToken token)
    throws AuthenticationException
  {
    return helper.getAuthenticationInfo(token);
  }

  /**
   * Method description
   *
   *
   * @param principals
   *
   * @return
   */
  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals)
  {
    AuthorizationInfo info = collector.collect(principals);
    
    Scope scope = principals.oneByType(Scope.class);
    if (scope != null && ! scope.isEmpty()) {
      LOG.trace("filter permissions by scope {}", scope);
      AuthorizationInfo filtered = Scopes.filter(getPermissionResolver(), info, scope);
      if (LOG.isTraceEnabled()) {
        log(principals, info, filtered);
      }
      return filtered;
    } else if (LOG.isTraceEnabled()) {
      LOG.trace("principal does not contain scope informations, returning all permissions");
      log(principals, info, null);
    }
    
    return info;
  }
  
  private void log( PrincipalCollection collection, AuthorizationInfo original, AuthorizationInfo filtered ) {
    StringBuilder buffer = new StringBuilder("authorization summary: ");
    
    buffer.append(SEPARATOR).append("username   : ").append(collection.getPrimaryPrincipal());
    buffer.append(SEPARATOR).append("groups     : ");
    append(buffer, collection.oneByType(GroupNames.class));
    buffer.append(SEPARATOR).append("roles      : ");
    append(buffer, original.getRoles()); 
    buffer.append(SEPARATOR).append("scope      : ");
    append(buffer, collection.oneByType(Scope.class)); 
    
    if ( filtered != null ) {
      buffer.append(SEPARATOR).append("permissions (filtered by scope): ");
      append(buffer, filtered);
      buffer.append(SEPARATOR).append("permissions (unfiltered): ");
    } else {
      buffer.append(SEPARATOR).append("permissions: ");
    }
    append(buffer, original);
    
    LOG.trace(buffer.toString());
  }
  
  private void append(StringBuilder buffer, AuthorizationInfo authz) {
    append(buffer, authz.getStringPermissions());
    append(buffer, authz.getObjectPermissions());    
  }
  
  private void append(StringBuilder buffer, Iterable<?> iterable){
    if (iterable != null){
      for ( Object item : iterable )
      {
        buffer.append(SEPARATOR).append(" - ").append(item);
      }      
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** default authorization collector */
  private final DefaultAuthorizationCollector collector;

  /** realm helper */
  private final DAORealmHelper helper;
}
