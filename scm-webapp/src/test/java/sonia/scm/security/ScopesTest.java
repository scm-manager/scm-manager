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
import com.google.common.collect.Sets;
import java.util.Set;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.apache.shiro.authz.permission.WildcardPermissionResolver;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * Unit tests for {@link Scopes}.
 * 
 * @author Sebastian Sdorra
 */
public class ScopesTest {

  private final WildcardPermissionResolver resolver = new WildcardPermissionResolver();

  /**
   * Tests that filter keep roles.
   */
  @Test
  public void testFilterKeepRoles(){
    AuthorizationInfo authz = authz("repository:read:123");
    
    AuthorizationInfo filtered = Scopes.filter(resolver, authz, Scope.empty());
    assertThat(filtered.getRoles(), containsInAnyOrder("unit", "test"));
  }
  
  /**
   * Tests filter with a simple allow.
   */
  @Test
  public void testFilterSimpleAllow() {
    Scope scope = Scope.valueOf("repository:read:123");
    AuthorizationInfo authz = authz("repository:*", "user:*:me");
    
    assertPermissions(Scopes.filter(resolver, authz, scope), "repository:read:123");
  }
  
  /**
   * Tests filter with a simple deny.
   */
  @Test
  public void testFilterSimpleDeny() {
    Scope scope = Scope.valueOf("repository:read:123");
    AuthorizationInfo authz = authz("user:*:me");
    
    AuthorizationInfo filtered = Scopes.filter(resolver, authz, scope);
    assertThat(filtered.getStringPermissions(), is(nullValue()));
    assertThat(filtered.getObjectPermissions(), is(emptyCollectionOf(Permission.class)));
  }
  
  /**
   * Tests filter with a multiple scope entries.
   */
  @Test
  public void testFilterMultiple() {
    Scope scope = Scope.valueOf("repo:read,modify:1", "repo:read:2", "repo:*:3", "repo:modify:4");
    AuthorizationInfo authz = authz("repo:read:*");
    
    assertPermissions(Scopes.filter(resolver, authz, scope), "repo:read:2");
  }
  
  /**
   * Tests filter with admin permissions.
   */
  @Test
  public void testFilterAdmin(){
    Scope scope = Scope.valueOf("repository:*", "user:*:me");
    AuthorizationInfo authz = authz("*");
    
    assertPermissions(Scopes.filter(resolver, authz, scope), "repository:*", "user:*:me");
  }
  
 /**
   * Tests filter with requested admin permissions from a non admin.
   */
  @Test
  public void testFilterRequestAdmin(){
    Scope scope = Scope.valueOf("*");
    AuthorizationInfo authz = authz("repository:*");
    
    assertThat(
      Scopes.filter(resolver, authz, scope).getObjectPermissions(),
      is(emptyCollectionOf(Permission.class))
    );
  }
  
  private void assertPermissions(AuthorizationInfo authz, Object... permissions) {
    assertThat(authz.getStringPermissions(), is(nullValue()));
    assertThat(
      Collections2.transform(authz.getObjectPermissions(), Permission::toString), 
      containsInAnyOrder(permissions)
    );
  }
  
  private AuthorizationInfo authz( String... values ) {
    SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(Sets.newHashSet("unit", "test"));
    Set<Permission> permissions = Sets.newLinkedHashSet();
    for ( String value : values ) {
      permissions.add(new WildcardPermission(value));
    }
    info.setObjectPermissions(permissions);
    return info;
  }

}