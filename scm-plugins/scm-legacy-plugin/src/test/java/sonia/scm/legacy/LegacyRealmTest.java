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
