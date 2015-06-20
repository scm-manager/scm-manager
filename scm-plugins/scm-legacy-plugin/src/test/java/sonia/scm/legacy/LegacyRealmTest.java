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



package sonia.scm.legacy;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.Sha1Hash;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import sonia.scm.group.GroupDAO;
import sonia.scm.security.BearerAuthenticationToken;
import sonia.scm.user.User;
import sonia.scm.user.UserDAO;
import sonia.scm.user.UserTestData;

import static org.hamcrest.Matchers.*;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

/**
 *
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class LegacyRealmTest
{

  /** Field description */
  private static final String NEW_PASSWORD =
    "$shiro1$SHA-512$8192$$yrNahBVDa4Gz+y5gat4msdjyvjtHlVE+N5nTl4WIDhtBFwhSIib13mKJt1sWmVqgHDWi3VwX7fkdkJ2+WToTbw==";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Test
  public void testDoGetAuthenticationInfo()
  {
    User user = UserTestData.createTrillian();

    user.setPassword(new Sha1Hash("secret").toHex());
    when(userDAO.get("tricia")).thenReturn(user);

    AuthenticationToken token = new UsernamePasswordToken("tricia", "secret");
    AuthenticationInfo authInfo = realm.doGetAuthenticationInfo(token);

    assertNotNull(authInfo);
    assertEquals("tricia", authInfo.getPrincipals().getPrimaryPrincipal());
  }

  /**
   * Method description
   *
   */
  @Test
  public void testDoGetAuthenticationInfoWithNewPasswords()
  {
    User user = UserTestData.createTrillian();
    user.setPassword(NEW_PASSWORD);
    when(userDAO.get("tricia")).thenReturn(user);
    
    AuthenticationToken token = new UsernamePasswordToken("tricia",
                                  NEW_PASSWORD);

    assertNull(realm.doGetAuthenticationInfo(token));
  }
  
/**
   * Method description
   *
   */
  @Test
  public void testDoGetAuthenticationInfoWithNullPassword()
  {
    when(userDAO.get("tricia")).thenReturn(UserTestData.createTrillian());
    AuthenticationToken token = new UsernamePasswordToken("tricia", "secret");

    assertNull(realm.doGetAuthenticationInfo(token));
  }

  /**
   * Method description
   *
   */
  @Test(expected = IllegalArgumentException.class)
  public void testDoGetAuthenticationInfoWrongToken()
  {
    realm.doGetAuthenticationInfo(new BearerAuthenticationToken("test"));
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Mock
  private GroupDAO groupDAO;

  /** Field description */
  @InjectMocks
  private LegacyRealm realm;

  /** Field description */
  @Mock
  private UserDAO userDAO;
}
