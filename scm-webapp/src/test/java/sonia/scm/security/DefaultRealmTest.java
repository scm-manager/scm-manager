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

//~--- non-JDK imports --------------------------------------------------------

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
import org.hamcrest.Matchers;
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

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultRealmTest
{

  /**
   * Method description
   *
   */
  @Test(expected = DisabledAccountException.class)
  public void testDisabledAccount()
  {
    User user = UserTestData.createMarvin();

    user.setActive(false);

    UsernamePasswordToken token = daoUser(user, "secret");

    realm.getAuthenticationInfo(token);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testGetAuthorizationInfo()
  {
    SimplePrincipalCollection col = new SimplePrincipalCollection();

    realm.doGetAuthorizationInfo(col);
    verify(collector, times(1)).collect(col);
  }
  
  /**
   * Tests {@link DefaultRealm#doGetAuthorizationInfo(PrincipalCollection)} without scope.
   */
  @Test
  public void testGetAuthorizationInfoWithoutScope(){
    SimplePrincipalCollection col = new SimplePrincipalCollection();
    
    SimpleAuthorizationInfo collectorsAuthz = new SimpleAuthorizationInfo();
    collectorsAuthz.addStringPermission("repository:*");
    when(collector.collect(col)).thenReturn(collectorsAuthz);
    
    AuthorizationInfo realmsAutz = realm.doGetAuthorizationInfo(col);
    assertThat(realmsAutz.getObjectPermissions(), is(nullValue()));
    assertThat(realmsAutz.getStringPermissions(), Matchers.contains("repository:*"));
  }

  @Test
  public void testGetAuthorizationInfoWithMultipleAuthorizationCollectors(){
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
    assertThat(realmsAuthz.getObjectPermissions(), contains(permission));
    assertThat(realmsAuthz.getStringPermissions(), containsInAnyOrder("repository:*", "user:*"));
    assertThat(realmsAuthz.getRoles(), Matchers.contains("awesome"));
  }

  /**
   * Tests {@link DefaultRealm#doGetAuthorizationInfo(PrincipalCollection)} with empty scope.
   */  
  @Test
  public void testGetAuthorizationInfoWithEmptyScope(){
    SimplePrincipalCollection col = new SimplePrincipalCollection();
    col.add(Scope.empty(), DefaultRealm.REALM);
    
    SimpleAuthorizationInfo collectorsAuthz = new SimpleAuthorizationInfo();
    collectorsAuthz.addStringPermission("repository:*");
    when(collector.collect(col)).thenReturn(collectorsAuthz);
    
    AuthorizationInfo realmsAutz = realm.doGetAuthorizationInfo(col);
    assertThat(realmsAutz.getObjectPermissions(), is(nullValue()));
    assertThat(realmsAutz.getStringPermissions(), Matchers.contains("repository:*"));
  }
  
  /**
   * Tests {@link DefaultRealm#doGetAuthorizationInfo(PrincipalCollection)} with scope.
   */
  @Test
  public void testGetAuthorizationInfoWithScope(){
    SimplePrincipalCollection col = new SimplePrincipalCollection();
    col.add(Scope.valueOf("user:*:me"), DefaultRealm.REALM);
    
    SimpleAuthorizationInfo collectorsAuthz = new SimpleAuthorizationInfo();
    collectorsAuthz.addStringPermission("repository:*");
    collectorsAuthz.addStringPermission("user:*:me");
    when(collector.collect(col)).thenReturn(collectorsAuthz);
    
    AuthorizationInfo realmsAutz = realm.doGetAuthorizationInfo(col);
    assertThat(
      Collections2.transform(realmsAutz.getObjectPermissions(), Permission::toString), 
      allOf(
        Matchers.contains("user:*:me"),
        not(Matchers.contains("repository:*"))
      )
    );
  }

  /**
   * Method description
   *
   */
  @Test
  public void testSimpleAuthentication()
  {
    User user = UserTestData.createTrillian();
    UsernamePasswordToken token = daoUser(user, "secret");
    AuthenticationInfo info = realm.getAuthenticationInfo(token);

    assertNotNull(info);

    PrincipalCollection collection = info.getPrincipals();

    assertEquals(token.getUsername(), collection.getPrimaryPrincipal());
    assertThat(collection.getRealmNames(), hasSize(1));
    assertThat(collection.getRealmNames(), hasItem(DefaultRealm.REALM));
    assertEquals(user, collection.oneByType(User.class));
  }

  /**
   * Method description
   *
   */
  @Test(expected = UnknownAccountException.class)
  public void testUnknownAccount()
  {
    realm.getAuthenticationInfo(new UsernamePasswordToken("tricia", "secret"));
  }

  /**
   * Method description
   *
   */
  @Test(expected = IllegalArgumentException.class)
  public void testWithoutUsername()
  {
    realm.getAuthenticationInfo(new UsernamePasswordToken(null, "secret"));
  }

  /**
   * Method description
   *
   */
  @Test(expected = IncorrectCredentialsException.class)
  public void testWrongCredentials()
  {
    UsernamePasswordToken token = daoUser(UserTestData.createDent(), "secret");

    token.setPassword("secret123".toCharArray());
    realm.getAuthenticationInfo(token);
  }

  /**
   * Method description
   *
   */
  @Test(expected = IllegalArgumentException.class)
  public void testWrongToken()
  {
    realm.getAuthenticationInfo(new OtherAuthenticationToken());
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   */
  @Before
  public void setUp()
  {
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

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param user
   * @param password
   *
   * @return
   */
  private UsernamePasswordToken daoUser(User user, String password)
  {
    user.setPassword(service.encryptPassword(password));
    when(userDAO.get(user.getName())).thenReturn(user);

    return new UsernamePasswordToken(user.getName(), password);
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 14/12/13
   * @author         Enter your name here...
   */
  private static class OtherAuthenticationToken implements AuthenticationToken
  {

    /** Field description */
    private static final long serialVersionUID = 8891352342377018022L;

    //~--- get methods --------------------------------------------------------

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public Object getCredentials()
    {
      throw new UnsupportedOperationException("Not supported yet.");    // To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public Object getPrincipal()
    {
      throw new UnsupportedOperationException("Not supported yet.");    // To change body of generated methods, choose Tools | Templates.
    }
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
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
  
  /** Field description */
  private DefaultRealm realm;

  /** Field description */
  private DefaultPasswordService service;
}
