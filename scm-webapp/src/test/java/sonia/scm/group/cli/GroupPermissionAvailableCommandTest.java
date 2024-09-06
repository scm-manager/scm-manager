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
import sonia.scm.cli.PermissionDescriptionResolver;
import sonia.scm.security.PermissionAssigner;
import sonia.scm.security.PermissionDescriptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupPermissionAvailableCommandTest {
  private final GroupTemplateTestRenderer testRenderer = new GroupTemplateTestRenderer();

  @Mock
  private PermissionAssigner permissionAssigner;
  @Mock
  private PermissionDescriptionResolver descriptionResolver;

  private GroupPermissionAvailableCommand command;

  @BeforeEach
  void initCommand() {
    command = new GroupPermissionAvailableCommand(testRenderer.getTemplateRenderer(), permissionAssigner, descriptionResolver);
  }

  @Test
  void shouldRenderAvailablePermissions() {
    when(permissionAssigner.getAvailablePermissions())
      .thenReturn(List.of(new PermissionDescriptor("hitchhiker"), new PermissionDescriptor("explorer")));
    when(descriptionResolver.getGlobalDescription("hitchhiker")).thenReturn(Optional.of("Hitchhikers Permission to the Galaxy"));

    command.run();

    assertThat(testRenderer.getStdOut()).contains(
      "VALUE      DESCRIPTION",
      "hitchhiker Hitchhikers Permission to the Galaxy",
      "explorer   explorer"
    );
  }
}
