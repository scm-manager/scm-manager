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
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.authz.permission.WildcardPermissionResolver;
import org.apache.shiro.crypto.hash.DefaultHashService;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.group.GroupDAO;
import sonia.scm.user.User;
import sonia.scm.user.UserDAO;
import sonia.scm.user.UserTestData;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultRealmTest {

  @Mock
  private DefaultAuthorizationCollector collector;

  private Set<AuthorizationCollector> authorizationCollectors;

  @Mock
  private LoginAttemptHandler loginAttemptHandler;

  @Mock
  private GroupDAO groupDAO;

  @Mock
  private UserDAO userDAO;

  @InjectMocks
  private DAORealmHelperFactory helperFactory;

  private DefaultRealm realm;

  private DefaultPasswordService service;


  @Test(expected = DisabledAccountException.class)
  public void testDisabledAccount() {
    User user = UserTestData.createMarvin();

    user.setActive(false);

    UsernamePasswordToken token = daoUser(user, "secret");

    realm.getAuthenticationInfo(token);
  }

  @Test
  public void testGetAuthorizationInfo() {
    SimplePrincipalCollection col = new SimplePrincipalCollection();

    realm.doGetAuthorizationInfo(col);
    verify(collector, times(1)).collect(col);
  }

  @Test
  public void testGetAuthorizationInfoWithoutScope() {
    SimplePrincipalCollection col = new SimplePrincipalCollection();

    SimpleAuthorizationInfo collectorsAuthz = new SimpleAuthorizationInfo();
    collectorsAuthz.addStringPermission("repository:*");
    when(collector.collect(col)).thenReturn(collectorsAuthz);

    AuthorizationInfo realmsAutz = realm.doGetAuthorizationInfo(col);
    assertThat(realmsAutz.getObjectPermissions()).isNull();
    assertThat(realmsAutz.getStringPermissions()).contains("repository:*");
  }

  @Test
  public void testGetAuthorizationInfoWithMultipleAuthorizationCollectors() {
    SimplePrincipalCollection col = new SimplePrincipalCollection();
    col.add(Scope.empty(), DefaultRealm.REALM);

    SimpleAuthorizationInfo collectedFromDefault = new SimpleAuthorizationInfo();
    collectedFromDefault.addStringPermission("repository:*");
    when(collector.collect(col)).thenReturn(collectedFromDefault);

    SimpleAuthorizationInfo collectedFromSecond = new SimpleAuthorizationInfo();
    collectedFromSecond.addStringPermission("user:*");
    collectedFromSecond.addRole("awesome");

    AuthorizationCollector secondCollector = principalCollection -> collectedFromSecond;
    authorizationCollectors.add(secondCollector);

    SimpleAuthorizationInfo collectedFromThird = new SimpleAuthorizationInfo();
    Permission permission = p -> false;
    collectedFromThird.addObjectPermission(permission);
    collectedFromThird.addRole("awesome");

    AuthorizationCollector thirdCollector = principalCollection -> collectedFromThird;
    authorizationCollectors.add(thirdCollector);

    AuthorizationInfo realmsAuthz = realm.doGetAuthorizationInfo(col);
    assertThat(realmsAuthz.getObjectPermissions()).contains(permission);
    assertThat(realmsAuthz.getStringPermissions()).containsExactlyInAnyOrder("repository:*", "user:*");
    assertThat(realmsAuthz.getRoles()).contains("awesome");
  }

  @Test
  public void testGetAuthorizationInfoWithEmptyScope() {
    SimplePrincipalCollection col = new SimplePrincipalCollection();
    col.add(Scope.empty(), DefaultRealm.REALM);

    SimpleAuthorizationInfo collectorsAuthz = new SimpleAuthorizationInfo();
    collectorsAuthz.addStringPermission("repository:*");
    when(collector.collect(col)).thenReturn(collectorsAuthz);

    AuthorizationInfo realmsAutz = realm.doGetAuthorizationInfo(col);
    assertThat(realmsAutz.getObjectPermissions()).isNull();
    ;
    assertThat(realmsAutz.getStringPermissions()).contains("repository:*");
  }

  @Test
  public void testGetAuthorizationInfoWithScope() {
    SimplePrincipalCollection col = new SimplePrincipalCollection();
    col.add(Scope.valueOf("user:*:me"), DefaultRealm.REALM);

    SimpleAuthorizationInfo collectorsAuthz = new SimpleAuthorizationInfo();
    collectorsAuthz.addStringPermission("repository:*");
    collectorsAuthz.addStringPermission("user:*:me");
    when(collector.collect(col)).thenReturn(collectorsAuthz);

    AuthorizationInfo realmsAutz = realm.doGetAuthorizationInfo(col);
    assertThat(Collections2.transform(realmsAutz.getObjectPermissions(), Permission::toString)).contains("user:*:me").doesNotContain("repository:*");
  }

  @Test
  public void testSimpleAuthentication() {
    User user = UserTestData.createTrillian();
    UsernamePasswordToken token = daoUser(user, "secret");
    AuthenticationInfo info = realm.getAuthenticationInfo(token);

    assertThat(info).isNotNull();

    PrincipalCollection collection = info.getPrincipals();

    assertThat(token.getUsername()).isEqualTo(collection.getPrimaryPrincipal());
    assertThat(collection.getRealmNames()).hasSize(1);
    assertThat(collection.getRealmNames()).contains(DefaultRealm.REALM);
    assertThat(user).isEqualTo(collection.oneByType(User.class));
  }

  @Test(expected = UnknownAccountException.class)
  public void testUnknownAccount() {
    realm.getAuthenticationInfo(new UsernamePasswordToken("tricia", "secret"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWithoutUsername() {
    realm.getAuthenticationInfo(new UsernamePasswordToken(null, "secret"));
  }

  @Test(expected = IncorrectCredentialsException.class)
  public void testWrongCredentials() {
    UsernamePasswordToken token = daoUser(UserTestData.createDent(), "secret");

    token.setPassword("secret123".toCharArray());
    realm.getAuthenticationInfo(token);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongToken() {
    realm.getAuthenticationInfo(new OtherAuthenticationToken());
  }

  @Before
  public void setUp() {
    service = new DefaultPasswordService();

    DefaultHashService hashService = new DefaultHashService();

    // use a small number of iterations for faster test execution
    hashService.setHashIterations(512);
    service.setHashService(hashService);

    authorizationCollectors = new HashSet<>();
    authorizationCollectors.add(collector);

    realm = new DefaultRealm(service, authorizationCollectors, helperFactory);

    // set permission resolver
    realm.setPermissionResolver(new WildcardPermissionResolver());
  }

  private UsernamePasswordToken daoUser(User user, String password) {
    user.setPassword(service.encryptPassword(password));
    when(userDAO.get(user.getName())).thenReturn(user);

    return new UsernamePasswordToken(user.getName(), password);
  }

  private static class OtherAuthenticationToken implements AuthenticationToken {

    private static final long serialVersionUID = 8891352342377018022L;

    @Override
    public Object getCredentials() {
      throw new UnsupportedOperationException("Not supported yet.");    // To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getPrincipal() {
      throw new UnsupportedOperationException("Not supported yet.");    // To change body of generated methods, choose Tools | Templates.
    }
  }
}
