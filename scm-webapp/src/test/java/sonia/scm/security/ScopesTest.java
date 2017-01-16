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