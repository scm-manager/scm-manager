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

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleAuthorizationInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Util methods for {@link Scope}.
 *
 * @since 2.0.0
 */
public final class Scopes {

  /** Key of scope in the claims of a token **/
  public final static String CLAIMS_KEY = "scope";

  private Scopes() {
  }

  /**
   * Returns scope from a token claims. If the claims does not contain a scope object, the method will return an empty
   * scope.
   *
   * @param claims token claims
   *
   * @return scope of claims
   */
  @SuppressWarnings("unchecked")
  public static Scope fromClaims(Map<String,Object> claims) {
    Scope scope = Scope.empty();
    if (claims.containsKey(Scopes.CLAIMS_KEY)) {
      scope = Scope.valueOf((Iterable<String>)claims.get(Scopes.CLAIMS_KEY));
    }
    return scope;
  }

  /**
   * Adds a scope to a token claims. The method will add the scope to the claims, if the scope is non null and not
   * empty.
   *
   * @param claims token claims
   * @param scope scope
   */
  public static void toClaims(Map<String,Object> claims, Scope scope) {
    if (scope != null && ! scope.isEmpty()) {
      claims.put(CLAIMS_KEY, ImmutableSet.copyOf(scope));
    }
  }

  /**
   * Limit permissions from {@link AuthorizationInfo} by scope values. Permission definitions from the
   * {@link AuthorizationInfo} will be returned, if a permission from the scope implies the original permission.
   * If a permission from the {@link AuthorizationInfo} exceeds the permissions defined by the scope, it will
   * be reduced. If the latter computation results in an empty permission, it will be omitted.
   *
   * @param resolver permission resolver
   * @param authz authorization info
   * @param scope scope
   *
   * @return limited {@link AuthorizationInfo}
   */
  public static AuthorizationInfo filter(ScmPermissionResolver resolver, AuthorizationInfo authz, Scope scope) {
    List<Permission> authzPermissions = authzPermissions(resolver, authz);
    Set<Permission> filteredPermissions = authzPermissions
      .stream()
      .map(p -> asScmWildcardPermission(p))
      .map(p -> p.limit(scope))
      .flatMap(Collection::stream)
      .collect(Collectors.toSet());

    Set<String> roles = ImmutableSet.copyOf(nullToEmpty(authz.getRoles()));
    SimpleAuthorizationInfo authzFiltered = new SimpleAuthorizationInfo(roles);
    authzFiltered.setObjectPermissions(filteredPermissions);
    return authzFiltered;
  }

  public static ScmWildcardPermission asScmWildcardPermission(Permission p) {
    return p instanceof ScmWildcardPermission ? (ScmWildcardPermission) p : new ScmWildcardPermission(p.toString());
  }

  private static <T> Collection<T> nullToEmpty(Collection<T> collection) {
    return collection != null ? collection : Collections.emptySet();
  }

  private static Collection<ScmWildcardPermission> resolve(ScmPermissionResolver resolver, Collection<String> permissions) {
    return Collections2.transform(nullToEmpty(permissions), resolver::resolvePermission);
  }

  private static List<Permission> authzPermissions(ScmPermissionResolver resolver, AuthorizationInfo authz){
    List<Permission> authzPermissions = Lists.newArrayList();
    authzPermissions.addAll(nullToEmpty(authz.getObjectPermissions()));
    authzPermissions.addAll(resolve(resolver, authz.getStringPermissions()));
    return authzPermissions;
  }

}
