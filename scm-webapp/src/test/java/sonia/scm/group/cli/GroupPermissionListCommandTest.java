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

package sonia.scm.group.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.cli.CliExitException;
import sonia.scm.cli.PermissionDescriptionResolver;
import sonia.scm.group.Group;
import sonia.scm.group.GroupManager;
import sonia.scm.security.PermissionAssigner;
import sonia.scm.security.PermissionDescriptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class GroupPermissionListCommandTest {

  private final GroupTemplateTestRenderer testRenderer = new GroupTemplateTestRenderer();

  @Mock
  private GroupManager manager;
  @Mock
  private PermissionAssigner permissionAssigner;
  @Mock
  private PermissionDescriptionResolver descriptionResolver;

  private GroupPermissionListCommand command;

  @BeforeEach
  void initCommand() {
    command = new GroupPermissionListCommand(testRenderer.getTemplateRenderer(), permissionAssigner, descriptionResolver, manager);
  }

  @Test
  void shouldRenderErrorForUnknownGroup() {
    when(manager.get(any())).thenReturn(null);

    assertThrows(CliExitException.class, () -> command.run());

    assertThat(testRenderer.getStdErr()).contains("Could not find group");
  }

  @Test
  void shouldRenderPermissionDescription() {
    when(manager.get(any())).thenReturn(new Group());
    command.setName("mygroup");
    when(permissionAssigner.readPermissionsForGroup("mygroup")).thenReturn(List.of(new PermissionDescriptor("hitchhiker")));
    when(descriptionResolver.getGlobalDescription("hitchhiker")).thenReturn(Optional.of("The Hitchhikers Permission to the Galaxy"));

    command.run();

    assertThat(testRenderer.getStdOut()).contains("The Hitchhikers Permission to the Galaxy");
  }

  @Test
  void shouldRenderPermissionKeys() {
    when(manager.get(any())).thenReturn(new Group());
    command.setName("mygroup");
    command.setKeys(true);
    when(permissionAssigner.readPermissionsForGroup("mygroup")).thenReturn(List.of(new PermissionDescriptor("hitchhiker")));

    command.run();

    assertThat(testRenderer.getStdOut()).isEqualTo("hitchhiker\n");
  }
}
