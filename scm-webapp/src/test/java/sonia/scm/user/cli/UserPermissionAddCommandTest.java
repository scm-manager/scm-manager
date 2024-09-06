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

package sonia.scm.user.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.cli.CliExitException;
import sonia.scm.security.PermissionAssigner;
import sonia.scm.security.PermissionDescriptor;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPermissionAddCommandTest {

  private final UserTemplateTestRenderer testRenderer = new UserTemplateTestRenderer();

  @Mock
  private UserManager manager;
  @Mock
  private PermissionAssigner permissionAssigner;

  private UserPermissionAddCommand command;

  @BeforeEach
  void initCommand() {
    command = new UserPermissionAddCommand(testRenderer.getTemplateRenderer(), permissionAssigner, manager);
  }

  @Test
  void shouldRenderErrorForUnknownUser() {
    when(manager.get(any())).thenReturn(null);
    command.setName("trillian");
    command.setAddedPermissions(new String[]{"hitchhiker"});

    assertThrows(CliExitException.class, () -> command.run());

    assertThat(testRenderer.getStdErr()).contains("Could not find user");
  }

  @Test
  void shouldRenderErrorForUnknownPermission() {
    when(manager.get(any())).thenReturn(new User());
    command.setName("trillian");
    command.setAddedPermissions(new String[]{"hitchhiker"});

    assertThrows(CliExitException.class, () -> command.run());

    assertThat(testRenderer.getStdErr()).contains("Unknown permission: hitchhiker");
  }

  @Test
  void shouldAddPermissions() {
    when(manager.get(any())).thenReturn(new User());
    command.setName("trillian");
    command.setAddedPermissions(new String[]{"hitchhiker", "heartOfGold"});
    when(permissionAssigner.getAvailablePermissions())
      .thenReturn(List.of(new PermissionDescriptor("hitchhiker"), new PermissionDescriptor("heartOfGold")));

    command.run();

    verify(permissionAssigner).setPermissionsForUser(eq("trillian"), argThat(arg -> {
      assertThat(arg.stream().map(PermissionDescriptor::getValue)).containsExactly("hitchhiker", "heartOfGold");
      return true;
    }));
  }
}
