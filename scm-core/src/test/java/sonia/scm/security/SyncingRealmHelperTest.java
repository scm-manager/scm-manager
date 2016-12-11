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

import com.google.common.base.Throwables;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import sonia.scm.group.Group;
import sonia.scm.group.GroupException;
import sonia.scm.group.GroupManager;
import sonia.scm.group.GroupNames;
import sonia.scm.user.User;
import sonia.scm.user.UserException;
import sonia.scm.user.UserManager;
import sonia.scm.web.security.AdministrationContext;
import sonia.scm.web.security.PrivilegedAction;

import static org.hamcrest.Matchers.*;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

/**
 * Unit tests for {@link SyncingRealmHelper}.
 *
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class SyncingRealmHelperTest {

  @Mock
  private GroupManager groupManager;

  @Mock
  private UserManager userManager;

  private SyncingRealmHelper helper;

  /**
   * Mock {@link AdministrationContext} and create object under test.
   */
  @Before
  public void setUp() {
    AdministrationContext ctx = new AdministrationContext() {

      @Override
      public void runAsAdmin(PrivilegedAction action) {
        action.run();
      }

      @Override
      public void runAsAdmin(Class<? extends PrivilegedAction> actionClass) {
        try {
          runAsAdmin(actionClass.newInstance());
        }
        catch (IllegalAccessException | InstantiationException ex) {
          throw Throwables.propagate(ex);
        }
      }
    };

    helper = new SyncingRealmHelper(ctx, userManager, groupManager);
  }

  /**
   * Tests {@link SyncingRealmHelper#createAuthenticationInfo(String, User, String...)}.
   */
  @Test
  public void testCreateAuthenticationInfo() {
    User user = new User("tricia");
    AuthenticationInfo authInfo = helper.createAuthenticationInfo("unit-test",
      user, "heartOfGold");

    assertNotNull(authInfo);
    assertEquals("tricia", authInfo.getPrincipals().getPrimaryPrincipal());
    assertThat(authInfo.getPrincipals().getRealmNames(), hasItem("unit-test"));
    assertEquals(user, authInfo.getPrincipals().oneByType(User.class));

    GroupNames groups = authInfo.getPrincipals().oneByType(GroupNames.class);

    assertThat(groups, hasItem("heartOfGold"));
  }

  /**
   * Tests {@link SyncingRealmHelper#store(Group)}.
   *
   * @throws GroupException
   * @throws IOException
   */
  @Test
  public void testStoreGroupCreate() throws GroupException, IOException {
    Group group = new Group("unit-test", "heartOfGold");

    helper.store(group);
    verify(groupManager, times(1)).create(group);
  }

  /**
   * Tests {@link SyncingRealmHelper#store(Group)} with thrown {@link GroupException}.
   *
   * @throws GroupException
   * @throws IOException
   */
  @Test(expected = AuthenticationException.class)
  public void testStoreGroupFailure() throws GroupException, IOException {
    Group group = new Group("unit-test", "heartOfGold");

    doThrow(GroupException.class).when(groupManager).create(group);
    helper.store(group);
  }

  /**
   * Tests {@link SyncingRealmHelper#store(Group)} with an existing group.
   *
   * @throws GroupException
   * @throws IOException
   */
  @Test
  public void testStoreGroupModify() throws GroupException, IOException {
    Group group = new Group("unit-test", "heartOfGold");

    when(groupManager.get("heartOfGold")).thenReturn(group);

    helper.store(group);
    verify(groupManager, times(1)).modify(group);
  }

  /**
   * Tests {@link SyncingRealmHelper#store(User)}.
   *
   * @throws UserException
   * @throws IOException
   */
  @Test
  public void testStoreUserCreate() throws UserException, IOException {
    User user = new User("tricia");

    helper.store(user);
    verify(userManager, times(1)).create(user);
  }

  /**
   * Tests {@link SyncingRealmHelper#store(User)} with a thrown {@link UserException}.
   *
   * @throws UserException
   * @throws IOException
   */
  @Test(expected = AuthenticationException.class)
  public void testStoreUserFailure() throws UserException, IOException {
    User user = new User("tricia");

    doThrow(UserException.class).when(userManager).create(user);
    helper.store(user);
  }

  /**
   * Tests {@link SyncingRealmHelper#store(User)} with an existing user.
   *
   * @throws UserException
   * @throws IOException
   */
  @Test
  public void testStoreUserModify() throws UserException, IOException {
    when(userManager.contains("tricia")).thenReturn(Boolean.TRUE);

    User user = new User("tricia");

    helper.store(user);
    verify(userManager, times(1)).modify(user);
  }
}
