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

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.authz.permission.PermissionResolver;

/**
 * Util methods for {@link Scope}.
 * 
 * @author Sebastian Sdorra
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
   * Filter permissions from {@link AuthorizationInfo} by scope values. Only permission definitions from the scope will
   * be returned and only if a permission from the {@link AuthorizationInfo} implies the requested scope permission.
   * 
   * @param resolver permission resolver
   * @param authz authorization info
   * @param scope scope
   * 
   * @return filtered {@link AuthorizationInfo}
   */
  public static AuthorizationInfo filter(PermissionResolver resolver, AuthorizationInfo authz, Scope scope) {
    List<Permission> authzPermissions = authzPermissions(resolver, authz);
    Predicate<Permission> predicate = implies(authzPermissions);
    Set<Permission> filteredPermissions = resolve(resolver, ImmutableList.copyOf(scope))
      .stream()
      .filter(predicate)
      .collect(Collectors.toSet());
    
    Set<String> roles = ImmutableSet.copyOf(nullToEmpty(authz.getRoles()));
    SimpleAuthorizationInfo authzFiltered = new SimpleAuthorizationInfo(roles);
    authzFiltered.setObjectPermissions(filteredPermissions);
    return authzFiltered;
  }
  
  private static <T> Collection<T> nullToEmpty(Collection<T> collection) {
    return collection != null ? collection : Collections.emptySet();
  }
  
  private static Collection<Permission> resolve(PermissionResolver resolver, Collection<String> permissions) {
    return Collections2.transform(nullToEmpty(permissions), resolver::resolvePermission);
  }
  
  private static Predicate<Permission> implies(Iterable<Permission> authzPermissions){
    return (scopePermission) -> {
      for ( Permission authzPermission : authzPermissions ) {
        if (authzPermission.implies(scopePermission)) {
          return true;
        }
      }
      return false;
    };
  }
  
  private static List<Permission> authzPermissions(PermissionResolver resolver, AuthorizationInfo authz){
    List<Permission> authzPermissions = Lists.newArrayList();
    authzPermissions.addAll(nullToEmpty(authz.getObjectPermissions()));
    authzPermissions.addAll(resolve(resolver, authz.getStringPermissions()));
    return authzPermissions;
  }
  
}
