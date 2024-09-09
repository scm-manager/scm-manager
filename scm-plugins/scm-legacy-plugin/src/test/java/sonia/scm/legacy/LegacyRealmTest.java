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

package sonia.scm.legacy;


import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.Sha1Hash;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.group.GroupDAO;
import sonia.scm.security.BearerToken;
import sonia.scm.security.DAORealmHelperFactory;
import sonia.scm.security.LoginAttemptHandler;
import sonia.scm.user.User;
import sonia.scm.user.UserDAO;
import sonia.scm.user.UserTestData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LegacyRealmTest {

  private static final String NEW_PASSWORD =
    "$shiro1$SHA-512$8192$$yrNahBVDa4Gz+y5gat4msdjyvjtHlVE+N5nTl4WIDhtBFwhSIib13mKJt1sWmVqgHDWi3VwX7fkdkJ2+WToTbw==";

  @Mock
  private LoginAttemptHandler loginAttemptHandler;

  @Mock
  private UserDAO userDAO;

  @Mock
  private GroupDAO groupDAO;

  @InjectMocks
  private DAORealmHelperFactory helperFactory;

  private LegacyRealm realm;

  @Before
  public void prepareObjectUnderTest() {
    realm = new LegacyRealm(helperFactory);
  }

  @Test
  public void testDoGetAuthenticationInfo() {
    User user = UserTestData.createTrillian();

    user.setPassword(new Sha1Hash("secret").toHex());
    when(userDAO.get("tricia")).thenReturn(user);

    AuthenticationToken token = new UsernamePasswordToken("tricia", "secret");
    AuthenticationInfo authInfo = realm.doGetAuthenticationInfo(token);

    assertNotNull(authInfo);
    assertEquals("tricia", authInfo.getPrincipals().getPrimaryPrincipal());
  }

  @Test
  public void testDoGetAuthenticationInfoWithNewPasswords() {
    User user = UserTestData.createTrillian();

    user.setPassword(NEW_PASSWORD);
    when(userDAO.get("tricia")).thenReturn(user);

    AuthenticationToken token = new UsernamePasswordToken("tricia",
      NEW_PASSWORD);

    assertNull(realm.doGetAuthenticationInfo(token));
  }

  @Test
  public void testDoGetAuthenticationInfoWithNullPassword() {
    User trillian = UserTestData.createTrillian();
    trillian.setPassword(null);
    when(userDAO.get("tricia")).thenReturn(trillian);

    AuthenticationToken token = new UsernamePasswordToken("tricia", "secret");

    assertNull(realm.doGetAuthenticationInfo(token));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDoGetAuthenticationInfoWrongToken() {
    realm.doGetAuthenticationInfo(BearerToken.valueOf("test"));
  }
}
