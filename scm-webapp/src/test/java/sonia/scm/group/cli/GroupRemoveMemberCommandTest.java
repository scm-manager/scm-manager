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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.cli.CliExitException;
import sonia.scm.group.Group;
import sonia.scm.group.GroupManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupRemoveMemberCommandTest {

  private final GroupTemplateTestRenderer testRenderer = new GroupTemplateTestRenderer();

  @Mock
  private GroupManager manager;

  private GroupRemoveMemberCommand command;

  @BeforeEach
  void initCommand() {
    command = new GroupRemoveMemberCommand(testRenderer.getTemplateRenderer(), manager);
  }

  @Nested
  class ForSuccessfulModificationTest {

    private Group group;

    @BeforeEach
    void mockModification() {
      group = new Group("test", "hog", "zaphod", "marvin", "trillian");

      when(manager.get("hog")).thenAnswer(invocation -> group);
      doAnswer(invocation -> {
        Group modifiedGroup = invocation.getArgument(0, Group.class);
        modifiedGroup.setLastModified(1649662000000L);
        group = modifiedGroup;
        return null;
      }).when(manager).modify(any());

      command.setName("hog");
      command.setMembers(new String[]{"zaphod", "marvin"});
    }

    @Test
    void shouldModifyGroup() {
      command.run();

      verify(manager).modify(argThat(argument -> {
        assertThat(argument.getName()).isEqualTo("hog");
        assertThat(argument.getMembers()).contains("trillian");
        return true;
      }));
    }

    @Test
    void shouldPrintGroupAfterModification() {
      command.run();

      assertThat(testRenderer.getStdOut())
        .contains(
          "Name:          hog",
          "Members:       trillian",
          "Last Modified: 2022-04-11T07:26:40Z"
        );
      assertThat(testRenderer.getStdErr())
        .isEmpty();
    }
  }

  @Test
  void shouldFailIfGroupDoesNotExists() {
    when(manager.get("hog")).thenReturn(null);
    command.setName("hog");

    Assertions.assertThrows(
      CliExitException.class,
      () -> command.run()
    );

    assertThat(testRenderer.getStdOut())
      .isEmpty();
    assertThat(testRenderer.getStdErr())
      .isEqualTo("Could not find group\n");
  }
}
