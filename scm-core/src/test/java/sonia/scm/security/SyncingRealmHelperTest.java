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


import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import org.apache.shiro.authc.AuthenticationInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.AlreadyExistsException;
import sonia.scm.group.Group;
import sonia.scm.group.GroupManager;
import sonia.scm.user.ExternalUserConverter;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.web.security.AdministrationContext;
import sonia.scm.web.security.PrivilegedAction;

import java.io.IOException;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SyncingRealmHelper}.
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class SyncingRealmHelperTest {

  @Mock
  private GroupManager groupManager;

  @Mock
  private UserManager userManager;

  @Mock
  private ExternalUserConverter converter;

  private SyncingRealmHelper helper;

  private SyncingRealmHelper helperWithConverters;

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
    helperWithConverters = new SyncingRealmHelper(ctx, userManager, groupManager, ImmutableSet.of(converter));
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
    ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
    User user = new User("tricia");

    helper.store(user);
    verify(userManager, times(1)).create(userArgumentCaptor.capture());

    User value = userArgumentCaptor.getValue();
    assertEquals(user.getDisplayName(), value.getDisplayName());
    assertEquals(user.getName(), value.getName());
  }

  /**
   * Tests {@link SyncingRealmHelper#store(User)} with a thrown {@link AlreadyExistsException}.
   */
  @Test(expected = IllegalStateException.class)
  public void testStoreUserFailure() {
    ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
    User user = new User("tricia");

    doThrow(AlreadyExistsException.class).when(userManager).create(userArgumentCaptor.capture());
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

  /**
   * Tests {@link SyncingRealmHelper#store(User)} with an existing user.
   */
  @Test
  public void testConvertUser(){
    User zaphod = new User("zaphod");
    when(converter.convert(any())).thenReturn(zaphod);
    when(userManager.contains("tricia")).thenReturn(Boolean.TRUE);

    User user = new User("tricia");

    helperWithConverters.store(user);

    verify(converter).convert(user);
    verify(userManager, times(1)).modify(zaphod);
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
