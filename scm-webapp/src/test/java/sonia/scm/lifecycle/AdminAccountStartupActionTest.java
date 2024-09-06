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

package sonia.scm.lifecycle;

import com.google.common.collect.Lists;
import org.apache.shiro.authc.credential.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sonia.scm.SCMContext;
import sonia.scm.security.PermissionAssigner;
import sonia.scm.security.PermissionDescriptor;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.user.UserTestData;
import sonia.scm.web.security.AdministrationContext;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAccountStartupActionTest {

  @Mock
  private PasswordService passwordService;
  @Mock
  private UserManager userManager;
  @Mock
  private PermissionAssigner permissionAssigner;
  @Mock
  private RandomPasswordGenerator randomPasswordGenerator;
  @Mock
  private AdministrationContext context;

  AdminAccountStartupAction startupAction;

  ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

  @BeforeEach
  void mockAdminContext() {
    doAnswer(invocation -> {
      invocation.getArgument(0, PrivilegedStartupAction.class).run();
      return null;
    }).when(context).runAsAdmin(any(PrivilegedStartupAction.class));
  }

  @BeforeEach
  void setUpUserCaptor() {
    lenient().when(userManager.create(userCaptor.capture())).thenAnswer(i -> i.getArgument(0));
  }

  @Nested
  class WithPredefinedPassword {
    @BeforeEach
    void initPasswordGenerator() {
      lenient().when(passwordService.encryptPassword("password")).thenReturn("encrypted");
    }

    @Test
    void shouldCreateAdminAccountIfNoUserExistsAndAssignPermissions() {
      createStartupAction("scmadmin", "password", false);

      verifyAdminCreated("scmadmin");
      verifyAdminPermissionsAssigned("scmadmin");
      assertThat(startupAction.done()).isTrue();
    }

    @Test
    void shouldUseSpecifiedAdminUsername() {
      createStartupAction("arthur", "password", false);

      verifyAdminCreated("arthur");
      verifyAdminPermissionsAssigned("arthur");
      assertThat(startupAction.done()).isTrue();
    }

    @Test
    void shouldCreateAdminAccountIfOnlyAnonymousUserExistsAndAssignPermissions() {
      when(userManager.getAll()).thenReturn(Lists.newArrayList(SCMContext.ANONYMOUS));
      when(userManager.contains(SCMContext.USER_ANONYMOUS)).thenReturn(true);

      createStartupAction("scmadmin", "password", false);

      verifyAdminCreated("scmadmin");
      verifyAdminPermissionsAssigned("scmadmin");
      assertThat(startupAction.done()).isTrue();
    }

    @Test
    void shouldDoNothingOnSecondStart() {
      List<User> users = Lists.newArrayList(UserTestData.createTrillian());
      when(userManager.getAll()).thenReturn(users);

      createStartupAction("scmadmin", "password", false);

      verify(userManager, never()).create(any(User.class));
      verify(permissionAssigner, never()).setPermissionsForUser(anyString(), any());
      assertThat(startupAction.done()).isTrue();
    }
  }

  @Test
  void shouldCreateStartupToken() {
    lenient().when(randomPasswordGenerator.createRandomPassword()).thenReturn("random");
    when(userManager.getAll()).thenReturn(Collections.emptyList());

    createStartupAction("scmadmin", "", false);

    verify(userManager, never()).create(any(User.class));
    verify(permissionAssigner, never()).setPermissionsForUser(anyString(), any());
    assertThat(startupAction.done()).isFalse();
    assertThat(startupAction.isCorrectToken("random")).isTrue();
    assertThat(startupAction.isCorrectToken("wrong")).isFalse();
  }

  @Test
  @MockitoSettings(strictness = Strictness.LENIENT)
  void shouldSkipAdminAccountCreationIfPropertyIsSet() {
    createStartupAction("scmadmin", "scmadmin", true);

    verify(userManager, never()).create(any());
    verify(permissionAssigner, never()).setPermissionsForUser(anyString(), any());
  }

  private void createStartupAction(String user, String password, boolean skipAdminCreation) {
    startupAction = new AdminAccountStartupAction(user, password, skipAdminCreation, passwordService, userManager, permissionAssigner, randomPasswordGenerator, context);
  }

  private void verifyAdminPermissionsAssigned(String expectedUsername) {
    ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Collection<PermissionDescriptor>> permissionCaptor = ArgumentCaptor.forClass(Collection.class);
    verify(permissionAssigner).setPermissionsForUser(usernameCaptor.capture(), permissionCaptor.capture());
    String username = usernameCaptor.getValue();
    assertThat(username).isEqualTo(expectedUsername);
    PermissionDescriptor descriptor = permissionCaptor.getValue().iterator().next();
    assertThat(descriptor.getValue()).isEqualTo("*");
  }

  private void verifyAdminCreated(String expectedUsername) {
    User user = userCaptor.getValue();
    assertThat(user.getName()).isEqualTo(expectedUsername);
    assertThat(user.getPassword()).isEqualTo("encrypted");
  }
}
