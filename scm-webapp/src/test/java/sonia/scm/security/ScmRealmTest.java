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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Provider;

import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.subject.PrincipalCollection;

import org.junit.Test;

import sonia.scm.cache.MapCacheManager;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.group.Group;
import sonia.scm.group.GroupManager;
import sonia.scm.group.GroupNames;
import sonia.scm.repository.PermissionType;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryDAO;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.user.User;
import sonia.scm.user.UserDAO;
import sonia.scm.user.UserManager;
import sonia.scm.user.UserTestData;
import sonia.scm.web.security.AuthenticationManager;
import sonia.scm.web.security.AuthenticationResult;
import sonia.scm.web.security.AuthenticationState;

import static org.hamcrest.Matchers.*;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Sebastian Sdorra
 */
public class ScmRealmTest
{

  /**
   * Method description
   *
   */
  @Test(expected = UnknownAccountException.class)
  public void testAuthenticationWithUknownUser()
  {
    User trillian = createSampleUser();
    ScmRealm realm = createRealm(trillian);

    realm.getAuthenticationInfo(token("marvin", trillian.getPassword()));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testAuthorizationAdminPermissions()
  {
    User trillian = createSampleUser();

    trillian.setAdmin(true);

    AuthorizationInfo ai = authorizationInfo(trillian);
    Collection<Permission> permissions = ai.getObjectPermissions();

    assertNotNull(permissions);
    assertFalse(permissions.isEmpty());
    assertEquals(1, permissions.size());
    //J-
    assertTrue(
      permissions.contains(new RepositoryPermission("*", PermissionType.OWNER))
    );
    //J+
  }

  /**
   * Method description
   *
   */
  @Test
  public void testAuthorizationAdminRoles()
  {
    User trillian = createSampleUser();

    trillian.setAdmin(true);

    AuthorizationInfo aci = authorizationInfo(trillian);
    Collection<String> roles = aci.getRoles();

    assertNotNull(roles);
    assertEquals(2, roles.size());
    assertTrue(roles.contains(Role.ADMIN));
    assertTrue(roles.contains(Role.USER));
  }

  /**
   *  Method description
   *
   */
  @Test
  public void testAuthorizationDefaultUserPermissions()
  {
    User trillian = createSampleUser();

    AuthorizationInfo ai = authorizationInfo(trillian);
    Collection<Permission> permissions = ai.getObjectPermissions();

    assertNotNull(permissions);
    assertTrue(permissions.isEmpty());
  }

  /**
   * Method description
   *
   */
  @Test
  public void testAuthorizationGroupPermissions()
  {
    User trillian = createSampleUser();

    String g1 = "g1";
    String g2 = "g2";
    Group g3 = new Group("xml", "g3");
    Group g4 = new Group("xml", "g4");

    Repository r1 = RepositoryTestData.create42Puzzle();

    prepareRepo(r1, g1, PermissionType.READ, true);

    Repository r2 = RepositoryTestData.createHappyVerticalPeopleTransporter();

    prepareRepo(r2, g2, PermissionType.WRITE, true);

    Repository r3 = RepositoryTestData.createHeartOfGold();

    prepareRepo(r3, g3, PermissionType.OWNER);

    Repository r4 = RepositoryTestData.createRestaurantAtTheEndOfTheUniverse();

    Set<Repository> repositories = ImmutableSet.of(r1, r2, r3, r4);
    ScmRealm realm = createRealm(trillian, ImmutableSet.of(g1, g2),
                       ImmutableSet.of(g3, g4), repositories);
    AuthenticationInfo aui = realm.getAuthenticationInfo(token(trillian));
    AuthorizationInfo aci = realm.doGetAuthorizationInfo(aui.getPrincipals());

    Collection<Permission> permissions = aci.getObjectPermissions();

    assertNotNull(permissions);
    assertFalse(permissions.isEmpty());
    assertEquals(3, permissions.size());
    containPermission(permissions, r1, PermissionType.READ);
    containPermission(permissions, r2, PermissionType.WRITE);
    containPermission(permissions, r3, PermissionType.OWNER);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testAuthorizationUserPermissions()
  {
    User trillian = createSampleUser();
    Repository r1 = RepositoryTestData.create42Puzzle();

    prepareRepo(r1, trillian, PermissionType.READ);

    Repository r2 = RepositoryTestData.createHappyVerticalPeopleTransporter();

    prepareRepo(r2, trillian, PermissionType.WRITE);

    Repository r3 = RepositoryTestData.createHeartOfGold();

    prepareRepo(r3, trillian, PermissionType.OWNER);

    Repository r4 = RepositoryTestData.createRestaurantAtTheEndOfTheUniverse();

    Set<Repository> repositories = ImmutableSet.of(r1, r2, r3, r4);

    ScmRealm realm = createRealm(trillian, null, null, repositories);
    AuthenticationInfo aui = realm.getAuthenticationInfo(token(trillian));
    AuthorizationInfo aci = realm.doGetAuthorizationInfo(aui.getPrincipals());
    Collection<Permission> permissions = aci.getObjectPermissions();

    assertNotNull(permissions);
    assertFalse(permissions.isEmpty());
    assertEquals(3, permissions.size());
    containPermission(permissions, r1, PermissionType.READ);
    containPermission(permissions, r2, PermissionType.WRITE);
    containPermission(permissions, r3, PermissionType.OWNER);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testAuthorizationUserRoles()
  {
    AuthorizationInfo aci = authorizationInfo(createSampleUser());
    Collection<String> roles = aci.getRoles();

    assertNotNull(roles);
    assertEquals(1, roles.size());
    assertEquals(Role.USER, roles.iterator().next());
  }

  /**
   * Method description
   *
   */
  @Test(expected = AccountException.class)
  public void testFailedAuthentication()
  {
    User trillian = createSampleUser();
    ScmRealm realm = createRealm(trillian);

    realm.getAuthenticationInfo(token(trillian.getId(), "hobbo"));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testSimpleAuthentication()
  {
    User trillian = createSampleUser();

    //J-
    ScmRealm realm = createRealm(
      trillian, 
      ImmutableSet.of("g1", "g2"), 
      ImmutableSet.of(new Group("xml", "g3"), new Group("xml", "g4")),
      null
    );
    //J+
    AuthenticationInfo ai = realm.getAuthenticationInfo(token(trillian));

    assertNotNull(ai);

    PrincipalCollection collection = ai.getPrincipals();

    assertNotNull(collection);
    assertFalse(collection.isEmpty());

    assertEquals(trillian.getId(), collection.getPrimaryPrincipal());
    assertEquals(trillian, collection.oneByType(User.class));

    GroupNames groups = collection.oneByType(GroupNames.class);

    assertNotNull(groups);
    assertFalse(groups.getCollection().isEmpty());
    assertEquals(4, groups.getCollection().size());
    assertThat(groups, containsInAnyOrder("g1", "g2", "g3", "g4"));
  }

  /**
   * Method description
   *
   *
   * @param user
   *
   * @return
   */
  private AuthorizationInfo authorizationInfo(User user)
  {
    ScmRealm realm = createRealm(user);
    AuthenticationInfo aui = realm.getAuthenticationInfo(token(user));
    AuthorizationInfo aci = realm.doGetAuthorizationInfo(aui.getPrincipals());

    assertNotNull(aci);

    return aci;
  }

  /**
   * Method description
   *
   *
   * @param permissions
   * @param repository
   * @param type
   */
  private void containPermission(Collection<Permission> permissions,
    Repository repository, PermissionType type)
  {
    assertTrue(
      permissions.contains(new RepositoryPermission(repository.getId(), type)));
  }

  /**
   * Method description
   *
   *
   * @param user
   *
   * @return
   */
  private ScmRealm createRealm(User user)
  {
    return createRealm(user, null, null, null);
  }

  /**
   * Method description
   *
   *
   *
   * @param user
   * @param authenticationGroups
   * @param dbGroups
   * @param repositories
   * @return
   */
  private ScmRealm createRealm(User user,
    Collection<String> authenticationGroups, Collection<Group> dbGroups,
    Collection<Repository> repositories)
  {
    UserManager userManager = mock(UserManager.class);
    GroupManager groupManager = mock(GroupManager.class);

    if (dbGroups != null)
    {
      when(groupManager.getGroupsForMember(user.getId())).thenReturn(dbGroups);
    }

    RepositoryDAO repositoryDAO = mock(RepositoryDAO.class);

    if (repositories != null)
    {
      when(repositoryDAO.getAll()).thenReturn(repositories);
    }

    UserDAO userDAO = mock(UserDAO.class);

    when(userDAO.get(user.getId())).thenReturn(user);

    HttpSession session = mock(HttpSession.class);

    final HttpServletRequest request = mock(HttpServletRequest.class);

    when(request.getSession(true)).thenReturn(session);

    Provider<HttpServletRequest> requestProvider =
      new Provider<HttpServletRequest>()
    {

      @Override
      public HttpServletRequest get()
      {
        return request;
      }
    };

    final HttpServletResponse response = mock(HttpServletResponse.class);
    Provider<HttpServletResponse> responseProvider =
      new Provider<HttpServletResponse>()
    {

      @Override
      public HttpServletResponse get()
      {
        return response;
      }
    };

    //J-
    AuthenticationManager authManager = mock(AuthenticationManager.class);
    
    when( 
      authManager.authenticate(
        eq(requestProvider.get()),
        eq(responseProvider.get()),
        eq(user.getId()),
        eq(user.getPassword())
      ) 
    ).thenReturn( 
      new AuthenticationResult(user, authenticationGroups, AuthenticationState.SUCCESS) 
    );
    
    when( 
      authManager.authenticate(
        eq(requestProvider.get()),
        eq(responseProvider.get()),
        eq(user.getId()),
        argThat(
          not(user.getPassword()) 
        )
      ) 
    ).thenReturn( 
      AuthenticationResult.FAILED
    );

    when( 
      authManager.authenticate(
        eq(requestProvider.get()),
        eq(responseProvider.get()),
        argThat(
          not(user.getName())
        ),
        anyString()
      ) 
    ).thenReturn( 
      AuthenticationResult.NOT_FOUND
    );
    
    SecuritySystem securitySystem = mock(SecuritySystem.class);
    when(
      securitySystem.getConfiguration()
    ).thenReturn(
      new SecurityConfiguration()
    );

    return new ScmRealm(
      new ScmConfiguration(),
      securitySystem,
      new MapCacheManager(),
      userManager,
      groupManager,
      repositoryDAO,
      userDAO,
      authManager,
      null,
      requestProvider,
      responseProvider
    );
    //J+
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private User createSampleUser()
  {
    User trillian = UserTestData.createTrillian();

    trillian.setPassword("moppo123");

    return trillian;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private String id()
  {
    return String.valueOf(counter.incrementAndGet());
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param user
   * @param type
   */
  private void prepareRepo(Repository repository, User user,
    PermissionType type)
  {
    prepareRepo(repository, user.getId(), type, false);
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param group
   * @param type
   */
  private void prepareRepo(Repository repository, Group group,
    PermissionType type)
  {
    prepareRepo(repository, group.getId(), type, true);
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param name
   * @param type
   * @param groupPermission
   */
  private void prepareRepo(Repository repository, String name,
    PermissionType type, boolean groupPermission)
  {
    repository.setId(id());

    List<sonia.scm.repository.Permission> permissions =
      ImmutableList.of(new sonia.scm.repository.Permission(name, type,
        groupPermission));

    repository.setPermissions(permissions);
  }

  /**
   * Method description
   *
   *
   * @param user
   *
   * @return
   */
  private AuthenticationToken token(User user)
  {
    return new UsernamePasswordToken(user.getId(), user.getPassword());
  }

  /**
   * Method description
   *
   *
   * @param username
   * @param password
   *
   * @return
   */
  private AuthenticationToken token(String username, String password)
  {
    return new UsernamePasswordToken(username, password);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private AtomicLong counter = new AtomicLong();
}
