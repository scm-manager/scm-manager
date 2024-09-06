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
import sonia.scm.group.Group;
import sonia.scm.group.GroupManager;
import sonia.scm.security.PermissionAssigner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupPermissionClearCommandTest {

  private final GroupTemplateTestRenderer testRenderer = new GroupTemplateTestRenderer();

  @Mock
  private GroupManager manager;
  @Mock
  private PermissionAssigner permissionAssigner;

  private GroupPermissionClearCommand command;

  @BeforeEach
  void initCommand() {
    command = new GroupPermissionClearCommand(testRenderer.getTemplateRenderer(), permissionAssigner, manager);
  }

  @Test
  void shouldRenderErrorForUnknownUser() {
    when(manager.get(any())).thenReturn(null);

    assertThrows(CliExitException.class, () -> command.run());

    assertThat(testRenderer.getStdErr()).isEqualTo("Could not find group\n");
  }

  @Test
  void shouldClearAllUserPermissions() {
    when(manager.get(any())).thenReturn(new Group());
    command.setName("hitchhikers");

    command.run();

    verify(permissionAssigner).setPermissionsForGroup(eq("hitchhikers"), argThat(arg -> {
      assertThat(arg).isEmpty();
      return true;
    }));
  }
}
