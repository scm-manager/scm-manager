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

package sonia.scm.lifecycle;

import com.google.common.collect.Lists;
import org.apache.shiro.authc.credential.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sonia.scm.SCMContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.security.PermissionAssigner;
import sonia.scm.security.PermissionDescriptor;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.user.UserTestData;
import sonia.scm.web.security.AdministrationContext;

import javax.inject.Provider;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
  @Mock
  private Provider<ScmPathInfoStore> scmPathInfoStore;

  @InjectMocks
  private AdminAccountStartupAction startupAction;

  @Nested
  class WIthNormalStartup {
    @BeforeEach
    void initPasswordGenerator() {
      System.clearProperty("scm.initialPassword");
      lenient().when(randomPasswordGenerator.createRandomPassword()).thenReturn("random");
      when(passwordService.encryptPassword("random")).thenReturn("secret");
    }

    @Test
    void shouldCreateAdminAccountIfNoUserExistsAndAssignPermissions() {
      startupAction.run();

      verifyAdminCreated();
      verifyAdminPermissionsAssigned();
    }

    @Test
    void shouldCreateAdminAccountIfOnlyAnonymousUserExistsAndAssignPermissions() {
      when(userManager.getAll()).thenReturn(Lists.newArrayList(SCMContext.ANONYMOUS));
      when(userManager.contains(SCMContext.USER_ANONYMOUS)).thenReturn(true);

      startupAction.run();

      verifyAdminCreated();
      verifyAdminPermissionsAssigned();
    }
  }

  @Test
  void shouldCreateAdminAccountWithGivenPassword() {
    System.setProperty("scm.initialPassword", "other_password");
    when(userManager.getAll()).thenReturn(Lists.newArrayList(SCMContext.ANONYMOUS));
    when(userManager.contains(SCMContext.USER_ANONYMOUS)).thenReturn(true);
    when(passwordService.encryptPassword("other_password")).thenReturn("secret");

    startupAction.run();

    verifyAdminCreated();
  }

  @Test
  @MockitoSettings(strictness = Strictness.LENIENT)
  void shouldSkipAdminAccountCreationIfPropertyIsSet() {
    System.setProperty("sonia.scm.skipAdminCreation", "true");

    try {
      startupAction.run();

      verify(userManager, never()).create(any());
      verify(permissionAssigner, never()).setPermissionsForUser(anyString(), any(Collection.class));
    } finally {
      System.setProperty("sonia.scm.skipAdminCreation", "");
    }
  }

  @Test
  void shouldDoNothingOnSecondStart() {
    List<User> users = Lists.newArrayList(UserTestData.createTrillian());
    when(userManager.getAll()).thenReturn(users);

    startupAction.run();

    verify(userManager, never()).create(any(User.class));
    verify(permissionAssigner, never()).setPermissionsForUser(anyString(), any(Collection.class));
  }

  private void verifyAdminPermissionsAssigned() {
    ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Collection<PermissionDescriptor>> permissionCaptor = ArgumentCaptor.forClass(Collection.class);
    verify(permissionAssigner).setPermissionsForUser(usernameCaptor.capture(), permissionCaptor.capture());
    String username = usernameCaptor.getValue();
    assertThat(username).isEqualTo("scmadmin");
    PermissionDescriptor descriptor = permissionCaptor.getValue().iterator().next();
    assertThat(descriptor.getValue()).isEqualTo("*");
  }

  private void verifyAdminCreated() {
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userManager).create(userCaptor.capture());
    User user = userCaptor.getValue();
    assertThat(user.getName()).isEqualTo("scmadmin");
    assertThat(user.getPassword()).isEqualTo("secret");
  }
}
