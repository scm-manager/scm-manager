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
import sonia.scm.group.Group;
import sonia.scm.group.GroupManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupDeleteCommandTest {

  @Mock
  private GroupManager manager;

  private final GroupTemplateTestRenderer testRenderer = new GroupTemplateTestRenderer();

  private GroupDeleteCommand command;

  @BeforeEach
  void initCommand() {
    command = new GroupDeleteCommand(testRenderer.getTemplateRenderer(), manager);
  }

  @Test
  void shouldRenderPromptWithoutYesFlag() {
    command.setName("hog");

    command.run();

    assertThat(testRenderer.getStdOut())
      .isEmpty();
    assertThat(testRenderer.getStdErr())
      .isEqualTo("If you really want to delete this group please pass --yes");
  }

  @Test
  void shouldDeleteGroup() {
    Group groupToDelete = new Group("test", "vogons");
    when(manager.get("vogons")).thenReturn(groupToDelete);
    command.setShouldDelete(true);
    command.setName("vogons");

    command.run();

    verify(manager).delete(groupToDelete);
    assertThat(testRenderer.getStdOut())
      .isEmpty();
    assertThat(testRenderer.getStdErr())
      .isEmpty();
  }

  @Test
  void shouldNotFailForNotExistingGroup() {
    when(manager.get("vogons")).thenReturn(null);
    command.setShouldDelete(true);
    command.setName("vogons");

    command.run();

    verify(manager, never()).delete(any());
    assertThat(testRenderer.getStdOut())
      .isEmpty();
    assertThat(testRenderer.getStdErr())
      .isEmpty();
  }
}
