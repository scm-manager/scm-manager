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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Throwables;
import org.apache.shiro.authc.AuthenticationInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.AlreadyExistsException;
import sonia.scm.group.Group;
import sonia.scm.group.GroupManager;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.web.security.AdministrationContext;
import sonia.scm.web.security.PrivilegedAction;

import java.io.IOException;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

//~--- JDK imports ------------------------------------------------------------

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
   * Tests {@link SyncingRealmHelper#store(Group)}.
   *
   * @throws IOException
   */
  @Test
  public void testStoreGroupCreate() {
    Group group = new Group("unit-test", "heartOfGold");

    helper.store(group);
    verify(groupManager, times(1)).create(group);
  }

  /**
   * Tests {@link SyncingRealmHelper#store(Group)}.
   */
  @Test(expected = IllegalStateException.class)
  public void testStoreGroupFailure() {
    Group group = new Group("unit-test", "heartOfGold");

    doThrow(AlreadyExistsException.class).when(groupManager).create(group);
    helper.store(group);
  }

  /**
   * Tests {@link SyncingRealmHelper#store(Group)} with an existing group.
   */
  @Test
  public void testStoreGroupModify(){
    Group group = new Group("unit-test", "heartOfGold");

    when(groupManager.get("heartOfGold")).thenReturn(group);

    helper.store(group);
    verify(groupManager, times(1)).modify(group);
  }

  /**
   * Tests {@link SyncingRealmHelper#store(User)}.
   *
   * @throws IOException
   */
  @Test
  public void testStoreUserCreate() {
    User user = new User("tricia");

    helper.store(user);
    verify(userManager, times(1)).create(user);
  }

  /**
   * Tests {@link SyncingRealmHelper#store(User)} with a thrown {@link AlreadyExistsException}.
   */
  @Test(expected = IllegalStateException.class)
  public void testStoreUserFailure() {
    User user = new User("tricia");

    doThrow(AlreadyExistsException.class).when(userManager).create(user);
    helper.store(user);
  }

  /**
   * Tests {@link SyncingRealmHelper#store(User)} with an existing user.
   */
  @Test
  public void testStoreUserModify(){
    when(userManager.contains("tricia")).thenReturn(Boolean.TRUE);

    User user = new User("tricia");

    helper.store(user);
    verify(userManager, times(1)).modify(user);
  }


  @Test
  public void builderShouldSetValues() {
    User user = new User("ziltoid");
    AuthenticationInfo authInfo = helper.createAuthenticationInfo("unit-test", user);

    assertNotNull(authInfo);
    assertEquals("ziltoid", authInfo.getPrincipals().getPrimaryPrincipal());
    assertThat(authInfo.getPrincipals().getRealmNames(), hasItem("unit-test"));
    assertEquals(user, authInfo.getPrincipals().oneByType(User.class));
  }
}
