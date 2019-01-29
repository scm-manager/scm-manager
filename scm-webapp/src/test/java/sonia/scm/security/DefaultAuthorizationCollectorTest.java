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

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.group.GroupNames;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryDAO;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.user.User;
import sonia.scm.user.UserTestData;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AuthorizationCollector}.
 *
 * @author Sebastian Sdorra
 */
@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class DefaultAuthorizationCollectorTest {

  private ScmConfiguration configuration;

  @Mock
  private Cache cache;

  @Mock
  private CacheManager cacheManager;

  @Mock
  private RepositoryDAO repositoryDAO;

  @Mock
  private SecuritySystem securitySystem;

  private DefaultAuthorizationCollector collector;

  @Rule
  public ShiroRule shiro = new ShiroRule();

  /**
   * Set up object to test.
   */
  @Before
  public void setUp(){
    when(cacheManager.getCache(Mockito.any(String.class))).thenReturn(cache);
    configuration = new ScmConfiguration();
    collector = new DefaultAuthorizationCollector(configuration, cacheManager, repositoryDAO, securitySystem);
  }

  @Test
  @SubjectAware(
    configuration = "classpath:sonia/scm/shiro-001.ini"
  )
  public void shouldGetAdminPrivilegedByConfiguration() {
    configuration.setAdminUsers(ImmutableSet.of("trillian"));
    authenticate(UserTestData.createTrillian(), "main");

    AuthorizationInfo authInfo = collector.collect();
    assertIsAdmin(authInfo);
  }

  private void assertIsAdmin(AuthorizationInfo authInfo) {
    assertThat(authInfo.getRoles(), Matchers.containsInAnyOrder(Role.USER, Role.ADMIN));
    assertThat(authInfo.getObjectPermissions(), nullValue());
    assertThat(authInfo.getStringPermissions(), Matchers.contains("*"));
  }

  @Test
  @SubjectAware(
    configuration = "classpath:sonia/scm/shiro-001.ini"
  )
  public void shouldGetAdminPrivilegedByGroupConfiguration() {
    configuration.setAdminGroups(ImmutableSet.of("heartOfGold"));
    authenticate(UserTestData.createTrillian(), "heartOfGold");

    AuthorizationInfo authInfo = collector.collect();
    assertIsAdmin(authInfo);
  }

  /**
   * Tests {@link AuthorizationCollector#collect()} without user role.
   */
  @Test
  @SubjectAware
  public void testCollectWithoutUserRole()
  {
    AuthorizationInfo authInfo = collector.collect();
    assertThat(authInfo.getRoles(), nullValue());
    assertThat(authInfo.getStringPermissions(), nullValue());
    assertThat(authInfo.getObjectPermissions(), nullValue());
  }

  /**
   * Tests {@link AuthorizationCollector#collect()} from cache.
   */
  @Test
  @SubjectAware(
    configuration = "classpath:sonia/scm/shiro-001.ini"
  )
  public void testCollectFromCache() {
    AuthorizationInfo info = new SimpleAuthorizationInfo();
    when(cache.get(anyObject())).thenReturn(info);
    authenticate(UserTestData.createTrillian(), "main");

    AuthorizationInfo authInfo = collector.collect();
    assertSame(info, authInfo);
  }

  /**
   * Tests {@link AuthorizationCollector#collect()} with cache.
   */
  @Test
  @SubjectAware(
    configuration = "classpath:sonia/scm/shiro-001.ini"
  )
  public void testCollectWithCache() {
    authenticate(UserTestData.createTrillian(), "main");

    collector.collect();
    verify(cache).put(any(), any());
  }

  /**
   * Tests {@link AuthorizationCollector#collect()} without permissions.
   */
  @Test
  @SubjectAware(
    configuration = "classpath:sonia/scm/shiro-001.ini"
  )
  public void testCollectWithoutPermissions() {
    authenticate(UserTestData.createTrillian(), "main");

    AuthorizationInfo authInfo = collector.collect();
    assertThat(authInfo.getRoles(), Matchers.contains(Role.USER));
    assertThat(authInfo.getStringPermissions(), hasSize(4));
    assertThat(authInfo.getStringPermissions(), containsInAnyOrder("user:autocomplete", "group:autocomplete", "user:changePassword:trillian", "user:read:trillian"));
    assertThat(authInfo.getObjectPermissions(), nullValue());
  }

  /**
   * Tests {@link AuthorizationCollector#collect()} as admin.
   */
  @Test
  @SubjectAware(
    configuration = "classpath:sonia/scm/shiro-001.ini"
  )
  public void testCollectAsAdmin() {
    User trillian = UserTestData.createTrillian();
    trillian.setAdmin(true);
    authenticate(trillian, "main");

    AuthorizationInfo authInfo = collector.collect();
    assertIsAdmin(authInfo);
  }

  /**
   * Tests {@link AuthorizationCollector#collect()} with repository permissions.
   */
  @Test
  @SubjectAware(
    configuration = "classpath:sonia/scm/shiro-001.ini"
  )
  public void testCollectWithRepositoryPermissions() {
    String group = "heart-of-gold-crew";
    authenticate(UserTestData.createTrillian(), group);
    Repository heartOfGold = RepositoryTestData.createHeartOfGold();
    heartOfGold.setId("one");
    heartOfGold.setPermissions(Lists.newArrayList(new RepositoryPermission("trillian", asList("read", "pull"), false)));
    Repository puzzle42 = RepositoryTestData.create42Puzzle();
    puzzle42.setId("two");
    RepositoryPermission permission = new RepositoryPermission(group, asList("read", "pull", "push"), true);
    puzzle42.setPermissions(Lists.newArrayList(permission));
    when(repositoryDAO.getAll()).thenReturn(Lists.newArrayList(heartOfGold, puzzle42));

    // execute and assert
    AuthorizationInfo authInfo = collector.collect();
    assertThat(authInfo.getRoles(), Matchers.containsInAnyOrder(Role.USER));
    assertThat(authInfo.getObjectPermissions(), nullValue());
    assertThat(authInfo.getStringPermissions(), containsInAnyOrder("user:autocomplete", "group:autocomplete", "user:changePassword:trillian", "repository:read,pull:one", "repository:read,pull,push:two", "user:read:trillian"));
  }

  /**
   * Tests {@link AuthorizationCollector#collect()} with global permissions.
   */
  @Test
  @SubjectAware(
    configuration = "classpath:sonia/scm/shiro-001.ini"
  )
  public void testCollectWithGlobalPermissions() {
    authenticate(UserTestData.createTrillian(), "main");

    StoredAssignedPermission p1 = new StoredAssignedPermission("one", new AssignedPermission("one", "one:one"));
    StoredAssignedPermission p2 = new StoredAssignedPermission("two", new AssignedPermission("two", "two:two"));
    when(securitySystem.getPermissions(any())).thenReturn(Lists.newArrayList(p1, p2));

    // execute and assert
    AuthorizationInfo authInfo = collector.collect();
    assertThat(authInfo.getRoles(), Matchers.containsInAnyOrder(Role.USER));
    assertThat(authInfo.getObjectPermissions(), nullValue());
    assertThat(authInfo.getStringPermissions(), containsInAnyOrder("one:one", "two:two", "user:read:trillian", "user:autocomplete", "group:autocomplete", "user:changePassword:trillian"));
  }

  private void authenticate(User user, String group, String... groups) {
    SimplePrincipalCollection spc = new SimplePrincipalCollection();
    spc.add(user.getName(), "unit");
    spc.add(user, "unit");
    spc.add(new GroupNames(group, groups), "unit");
    Subject subject = new Subject.Builder().authenticated(true).principals(spc).buildSubject();
    shiro.setSubject(subject);
  }

  /**
   * Tests {@link DefaultAuthorizationCollector#invalidateCache(sonia.scm.security.AuthorizationChangedEvent)}.
   */
  @Test
  public void testInvalidateCache() {
    collector.invalidateCache(AuthorizationChangedEvent.createForEveryUser());
    verify(cache).clear();

    collector.invalidateCache(AuthorizationChangedEvent.createForUser("dent"));
    verify(cache).removeAll(any());
  }

}
