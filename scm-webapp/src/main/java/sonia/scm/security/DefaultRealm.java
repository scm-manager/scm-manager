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

package sonia.scm.security;


import com.google.common.annotations.VisibleForTesting;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.PasswordMatcher;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.Extension;

import java.util.Iterator;
import java.util.Set;

/**
 * Default authorizing realm.
 *
 * @since 2.0.0
 */
@Extension
@Singleton
public class DefaultRealm extends AuthorizingRealm {

  private static final String SEPARATOR = System.getProperty("line.separator", "\n");
  private static final Logger LOG = LoggerFactory.getLogger(DefaultRealm.class);

  @VisibleForTesting
  static final String REALM = "DefaultRealm";
  private final ScmPermissionResolver permissionResolver;
  private final Set<AuthorizationCollector> authorizationCollectors;
  private final DAORealmHelper helper;

  @Inject
  public DefaultRealm(PasswordService service,
                      Set<AuthorizationCollector> authorizationCollectors,
                      DAORealmHelperFactory helperFactory) {
    this.authorizationCollectors = authorizationCollectors;
    this.helper = helperFactory.create(REALM);

    PasswordMatcher matcher = new PasswordMatcher();

    matcher.setPasswordService(service);
    setCredentialsMatcher(helper.wrapCredentialsMatcher(matcher));
    setAuthenticationTokenClass(UsernamePasswordToken.class);
    permissionResolver = new ScmPermissionResolver();
    setPermissionResolver(permissionResolver);

    // we cache in the AuthorizationCollector
    setCachingEnabled(false);
  }

  @Override
  public ScmPermissionResolver getPermissionResolver() {
    return permissionResolver;
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    return helper.getAuthenticationInfo(token);
  }

  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    AuthorizationInfo info = collectors(principals);
    Scope scope = principals.oneByType(Scope.class);
    if (scope != null && !scope.isEmpty()) {
      LOG.trace("filter permissions by scope {}", scope);
      AuthorizationInfo filtered = Scopes.filter(getPermissionResolver(), info, scope);
      if (LOG.isTraceEnabled()) {
        log(principals, info, filtered);
      }
      return filtered;
    } else if (LOG.isTraceEnabled()) {
      LOG.trace("principal does not contain scope information, returning all permissions");
      log(principals, info, null);
    }

    return info;
  }

  private AuthorizationInfo collectors(PrincipalCollection principals) {
    SimpleAuthorizationInfo merged = new SimpleAuthorizationInfo();
    for (AuthorizationCollector collector : authorizationCollectors) {
      AuthorizationInfo authorizationInfo = collector.collect(principals);
      merge(merged, authorizationInfo);
    }
    return merged;
  }

  private void merge(SimpleAuthorizationInfo merged, AuthorizationInfo authorizationInfo) {
    if (authorizationInfo != null) {
      if (authorizationInfo.getRoles() != null) {
        merged.addRoles(authorizationInfo.getRoles());
      }
      if (authorizationInfo.getObjectPermissions() != null) {
        merged.addObjectPermissions(authorizationInfo.getObjectPermissions());
      }
      if (authorizationInfo.getStringPermissions() != null) {
        merged.addStringPermissions(authorizationInfo.getStringPermissions());
      }
    }
  }

  private void log(PrincipalCollection collection, AuthorizationInfo original, AuthorizationInfo filtered) {
    StringBuilder buffer = new StringBuilder("authorization summary: ");

    buffer.append(SEPARATOR).append("username   : ").append(collection.getPrimaryPrincipal());
    buffer.append(SEPARATOR).append("roles      : ");
    append(buffer, original.getRoles());
    buffer.append(SEPARATOR).append("scope      : ");
    append(buffer, collection.oneByType(Scope.class));

    if (filtered != null) {
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

  private void append(StringBuilder buffer, Iterable<?> iterable) {
    if (iterable != null) {
      for (Iterator<?> iter = iterable.iterator(); iter.hasNext(); ) {
        buffer.append(iter.next());
        if (iter.hasNext()) {
          buffer.append(" , ");
        }
      }
    }
  }
}
