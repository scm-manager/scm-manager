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

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.common.collect.ImmutableSet;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;
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
import sonia.scm.SCMContext;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.group.GroupCollector;
import sonia.scm.repository.Namespace;
import sonia.scm.repository.NamespaceDao;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryDAO;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryRole;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.user.User;
import sonia.scm.user.UserTestData;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertSame;
import static org.hamcrest.MatcherAssert.assertThat;
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

  @Mock
  private Cache cache;

  @Mock
  private CacheManager cacheManager;

  @Mock
  private RepositoryDAO repositoryDAO;

  @Mock
  private SecuritySystem securitySystem;

  @Mock
  private RepositoryPermissionProvider repositoryPermissionProvider;

  @Mock
  private GroupCollector groupCollector;

  @Mock
  private NamespaceDao namespaceDao;

  private DefaultAuthorizationCollector collector;

  @Rule
  public ShiroRule shiro = new ShiroRule();

  /**
   * Set up object to test.
   */
  @Before
  public void setUp(){
    when(cacheManager.getCache(Mockito.any(String.class))).thenReturn(cache);
    collector = new DefaultAuthorizationCollector(cacheManager, repositoryDAO, securitySystem, repositoryPermissionProvider, groupCollector, namespaceDao);
  }

  /**
   * Tests {@link AuthorizationCollector#collect(PrincipalCollection)} ()} without user role.
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
   * Tests {@link AuthorizationCollector#collect(PrincipalCollection)} from cache.
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
    assertSame(info.getStringPermissions(), authInfo.getStringPermissions());
    assertSame(info.getObjectPermissions(), authInfo.getObjectPermissions());
    assertSame(info.getRoles(), authInfo.getRoles());
  }

  /**
   * Tests {@link AuthorizationCollector#collect(PrincipalCollection)} ()} with cache.
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
   * Tests {@link AuthorizationCollector#collect(PrincipalCollection)} ()} without permissions.
   */
  @Test
  @SubjectAware(
    configuration = "classpath:sonia/scm/shiro-001.ini"
  )
  public void testCollectWithoutPermissions() {
    authenticate(UserTestData.createTrillian(), "main");

    AuthorizationInfo authInfo = collector.collect();
    assertThat(authInfo.getRoles(), Matchers.contains(Role.USER));
    assertThat(authInfo.getStringPermissions(), hasSize(6));
    assertThat(authInfo.getStringPermissions(), containsInAnyOrder("user:autocomplete", "group:autocomplete", "user:changePassword:trillian", "user:read:trillian", "user:changeApiKeys:trillian", "user:changePublicKeys:trillian"));
    assertThat(authInfo.getObjectPermissions(), nullValue());
  }

  /**
   * Tests {@link AuthorizationCollector#collect(PrincipalCollection)} ()} without permissions.
   */
  @Test
  @SubjectAware(
    configuration = "classpath:sonia/scm/shiro-001.ini"
  )
  public void testCollectWithoutPermissionsForAnonymousUser() {
    authenticate(SCMContext.ANONYMOUS, "anon");

    AuthorizationInfo authInfo = collector.collect();
    assertThat(authInfo.getRoles(), Matchers.contains(Role.USER));
    assertThat(authInfo.getStringPermissions(), hasSize(1));
    assertThat(authInfo.getStringPermissions(), containsInAnyOrder("user:read:_anonymous"));
    assertThat(authInfo.getObjectPermissions(), nullValue());
  }

  /**
   * Tests {@link AuthorizationCollector#collect(PrincipalCollection)} ()} with repository permissions.
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
    heartOfGold.setPermissions(newArrayList(new RepositoryPermission("trillian", asList("read", "pull"), false)));
    Repository puzzle42 = RepositoryTestData.create42Puzzle();
    puzzle42.setId("two");
    RepositoryPermission permission = new RepositoryPermission(group, asList("read", "pull", "push"), true);
    puzzle42.setPermissions(newArrayList(permission));
    when(repositoryDAO.getAll()).thenReturn(newArrayList(heartOfGold, puzzle42));

    // execute and assert
    AuthorizationInfo authInfo = collector.collect();
    assertThat(authInfo.getRoles(), Matchers.containsInAnyOrder(Role.USER));
    assertThat(authInfo.getObjectPermissions(), nullValue());
    assertThat(authInfo.getStringPermissions(), containsInAnyOrder("user:autocomplete", "group:autocomplete", "user:changePassword:trillian", "repository:read,pull:one", "repository:read,pull,push:two", "user:read:trillian", "user:changeApiKeys:trillian", "user:changePublicKeys:trillian"));
  }

  /**
   * Tests {@link AuthorizationCollector#collect(PrincipalCollection)} ()} with repository permissions.
   */
  @Test
  @SubjectAware(
    configuration = "classpath:sonia/scm/shiro-001.ini"
  )
  public void testCollectWithNamespacePermissions() {
    String group = "heart-of-gold-crew";
    authenticate(UserTestData.createTrillian(), group);
    Repository heartOfGold = RepositoryTestData.createHeartOfGold();
    heartOfGold.setId("one");
    Namespace heartOfGoldNamespace = new Namespace(heartOfGold.getNamespace());
    heartOfGoldNamespace.setPermissions(newArrayList(new RepositoryPermission("trillian", asList("read", "pull"), false)));

    Repository puzzle42 = RepositoryTestData.create42Puzzle();
    puzzle42.setNamespace("guide");
    puzzle42.setId("two");
    Namespace puzzleNamespace = new Namespace(puzzle42.getNamespace());
    puzzleNamespace.setPermissions(newArrayList(new RepositoryPermission(group, asList("read", "pull", "push"), true)));

    when(repositoryDAO.getAll()).thenReturn(newArrayList(heartOfGold, puzzle42));
    when(namespaceDao.get(heartOfGold.getNamespace())).thenReturn(of(heartOfGoldNamespace));
    when(namespaceDao.get(puzzle42.getNamespace())).thenReturn(of(puzzleNamespace));
    when(namespaceDao.allWithPermissions()).thenReturn(asList(heartOfGoldNamespace, puzzleNamespace));

    // execute and assert
    AuthorizationInfo authInfo = collector.collect();
    assertThat(authInfo.getRoles(), Matchers.containsInAnyOrder(Role.USER));
    assertThat(authInfo.getObjectPermissions(), nullValue());
    assertThat(authInfo.getStringPermissions(), containsInAnyOrder(
      "user:autocomplete",
      "group:autocomplete",
      "user:changePassword:trillian",
      "repository:read,pull:one",
      "repository:read,pull,push:two",
      "namespace:read,pull:hitchhiker",
      "namespace:read,pull,push:guide",
      "user:read:trillian",
      "user:changeApiKeys:trillian",
      "user:changePublicKeys:trillian")
    );
  }

  /**
   * Tests {@link AuthorizationCollector#collect(PrincipalCollection)} with repository roles.
   */
  @Test
  @SubjectAware(
    configuration = "classpath:sonia/scm/shiro-001.ini"
  )
  public void testCollectWithRepositoryRolePermissions() {
    when(repositoryPermissionProvider.availableRoles()).thenReturn(
      asList(
        new RepositoryRole("user role", singletonList("user"), "xml"),
        new RepositoryRole("group role", singletonList("group"), "xml"),
        new RepositoryRole("system role", singletonList("system"), "system")
      ));

    String group = "heart-of-gold-crew";
    authenticate(UserTestData.createTrillian(), group);
    Repository heartOfGold = RepositoryTestData.createHeartOfGold();
    heartOfGold.setId("one");
    heartOfGold.setPermissions(newArrayList(
      new RepositoryPermission("trillian", "user role", false),
      new RepositoryPermission("trillian", "system role", false)
    ));
    Repository puzzle42 = RepositoryTestData.create42Puzzle();
    puzzle42.setId("two");
    RepositoryPermission permission = new RepositoryPermission(group, "group role", true);
    puzzle42.setPermissions(newArrayList(permission));
    when(repositoryDAO.getAll()).thenReturn(newArrayList(heartOfGold, puzzle42));

    // execute and assert
    AuthorizationInfo authInfo = collector.collect();
    assertThat(authInfo.getRoles(), Matchers.containsInAnyOrder(Role.USER));
    assertThat(authInfo.getObjectPermissions(), nullValue());
    assertThat(authInfo.getStringPermissions(), containsInAnyOrder(
      "user:autocomplete",
      "group:autocomplete",
      "user:changePassword:trillian",
      "repository:user:one",
      "repository:system:one",
      "repository:group:two",
      "user:read:trillian",
      "user:changeApiKeys:trillian",
      "user:changePublicKeys:trillian"
    ));
  }

  /**
   * Tests {@link AuthorizationCollector#collect(PrincipalCollection)} with repository roles.
   */
  @Test(expected = IllegalStateException.class)
  @SubjectAware(
    configuration = "classpath:sonia/scm/shiro-001.ini"
  )
  public void testCollectWithUnknownRepositoryRole() {
    when(repositoryPermissionProvider.availableRoles()).thenReturn(
      singletonList(
        new RepositoryRole("something", singletonList("something"), "xml")
      ));

    String group = "heart-of-gold-crew";
    authenticate(UserTestData.createTrillian(), group);
    Repository heartOfGold = RepositoryTestData.createHeartOfGold();
    heartOfGold.setId("one");
    heartOfGold.setPermissions(singletonList(
      new RepositoryPermission("trillian", "unknown", false)
    ));
    when(repositoryDAO.getAll()).thenReturn(newArrayList(heartOfGold));

    // execute and assert
    AuthorizationInfo authInfo = collector.collect();
  }

  /**
   * Tests {@link AuthorizationCollector#collect(PrincipalCollection)} ()} with global permissions.
   */
  @Test
  @SubjectAware(
    configuration = "classpath:sonia/scm/shiro-001.ini"
  )
  public void testCollectWithGlobalPermissions() {
    authenticate(UserTestData.createTrillian(), "main");

    StoredAssignedPermission p1 = new StoredAssignedPermission("one", new AssignedPermission("one", "one:one"));
    StoredAssignedPermission p2 = new StoredAssignedPermission("two", new AssignedPermission("two", "two:two"));
    when(securitySystem.getPermissions(any())).thenReturn(newArrayList(p1, p2));

    // execute and assert
    AuthorizationInfo authInfo = collector.collect();
    assertThat(authInfo.getRoles(), Matchers.containsInAnyOrder(Role.USER));
    assertThat(authInfo.getObjectPermissions(), nullValue());
    assertThat(authInfo.getStringPermissions(), containsInAnyOrder("one:one", "two:two", "user:read:trillian", "user:autocomplete", "group:autocomplete", "user:changePassword:trillian", "user:changeApiKeys:trillian", "user:changePublicKeys:trillian"));
  }

  private void authenticate(User user, String group, String... groups) {
    SimplePrincipalCollection spc = new SimplePrincipalCollection();
    spc.add(user.getName(), "unit");
    spc.add(user, "unit");
    Subject subject = new Subject.Builder().authenticated(true).principals(spc).buildSubject();
    shiro.setSubject(subject);

    ImmutableSet.Builder<String> builder = ImmutableSet.builder();
    builder.add(group);
    builder.add(groups);
    when(groupCollector.collect(user.getName())).thenReturn(builder.build());
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
