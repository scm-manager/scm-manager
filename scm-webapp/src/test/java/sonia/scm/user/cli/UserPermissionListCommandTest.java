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
import sonia.scm.cli.PermissionDescriptionResolver;
import sonia.scm.security.PermissionAssigner;
import sonia.scm.security.PermissionDescriptor;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPermissionListCommandTest {

  private final UserTemplateTestRenderer testRenderer = new UserTemplateTestRenderer();

  @Mock
  private UserManager manager;
  @Mock
  private PermissionAssigner permissionAssigner;
  @Mock
  private PermissionDescriptionResolver descriptionResolver;

  private UserPermissionListCommand command;

  @BeforeEach
  void initCommand() {
    command = new UserPermissionListCommand(testRenderer.getTemplateRenderer(), permissionAssigner, descriptionResolver, manager);
  }

  @Test
  void shouldRenderErrorForUnknownGroup() {
    when(manager.get(any())).thenReturn(null);

    assertThrows(CliExitException.class, () -> command.run());

    assertThat(testRenderer.getStdErr()).contains("Could not find user");
  }

  @Test
  void shouldRenderPermissionDescription() {
    when(manager.get(any())).thenReturn(new User());
    command.setName("trillian");
    when(permissionAssigner.readPermissionsForUser("trillian")).thenReturn(List.of(new PermissionDescriptor("hitchhiker")));
    when(descriptionResolver.getGlobalDescription("hitchhiker")).thenReturn(Optional.of("The Hitchhikers Permission to the Galaxy"));

    command.run();

    assertThat(testRenderer.getStdOut()).contains("The Hitchhikers Permission to the Galaxy");
  }

  @Test
  void shouldRenderPermissionKeys() {
    when(manager.get(any())).thenReturn(new User());
    command.setName("trillian");
    command.setKeys(true);
    when(permissionAssigner.readPermissionsForUser("trillian")).thenReturn(List.of(new PermissionDescriptor("hitchhiker")));

    command.run();

    assertThat(testRenderer.getStdOut()).isEqualTo("hitchhiker\n");
  }
}
