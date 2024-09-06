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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.group.Group;
import sonia.scm.group.GroupManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupCreateCommandTest {

  private final GroupTemplateTestRenderer testRenderer = new GroupTemplateTestRenderer();

  @Mock
  private GroupManager manager;

  private GroupCreateCommand command;

  @BeforeEach
  void initCommand() {
    command = new GroupCreateCommand(testRenderer.getTemplateRenderer(), manager);
  }

  @Nested
  class ForSuccessfulCreationTest {

    @BeforeEach
    void mockCreation() {
      when(manager.create(any()))
        .thenAnswer(invocation -> {
          Group createdGroup = invocation.getArgument(0, Group.class);
          createdGroup.setCreationDate(1649262000000L);
          return createdGroup;
        });

      command.setName("hog");
      command.setDescription("Crew of the Heart of Gold");
      command.setMembers(new String[]{"zaphod", "trillian"});
    }

    @Test
    void shouldCreateGroup() {
      command.run();

      verify(manager).create(argThat(argument -> {
        assertThat(argument.getName()).isEqualTo("hog");
        assertThat(argument.getDescription()).isEqualTo("Crew of the Heart of Gold");
        assertThat(argument.getMembers()).contains("zaphod", "trillian");
        return true;
      }));
    }

    @Test
    void shouldPrintGroupAfterCreation() {
      command.run();

      assertThat(testRenderer.getStdOut())
        .contains(
          "Name:          hog",
          "Description:   Crew of the Heart of Gold",
          "Members:       zaphod, trillian",
          "External:      no",
          "Creation Date: 2022-04-06T16:20:00Z",
          "Last Modified: "
        );
      assertThat(testRenderer.getStdErr())
        .isEmpty();
    }
  }
}
