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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.cli.CliExitException;
import sonia.scm.group.Group;
import sonia.scm.group.GroupManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupGetCommandTest {

  private final GroupTemplateTestRenderer testRenderer = new GroupTemplateTestRenderer();

  @Mock
  private GroupManager manager;

  private GroupGetCommand command;

  @BeforeEach
  void initCommand() {
    command = new GroupGetCommand(testRenderer.getTemplateRenderer(), manager);
  }

  @Test
  void shouldGetGroup() {
    Group group = new Group("test", "hog", "zaphod", "trillian");
    group.setCreationDate(1649262000000L);
    group.setLastModified(1649462000000L);
    group.setDescription("Crew of the Heart of Gold");

    when(manager.get("hog")).thenReturn(group);

    command.setName("hog");

    command.run();

    assertThat(testRenderer.getStdOut())
      .contains(
        "Name:          hog",
        "Description:   Crew of the Heart of Gold",
        "Members:       zaphod, trillian",
        "External:      no",
        "Creation Date: 2022-04-06T16:20:00Z",
        "Last Modified: 2022-04-08T23:53:20Z"
      );
    assertThat(testRenderer.getStdErr())
      .isEmpty();
  }

  @Test
  void shouldFailForNotExistingGroup() {
    command.setName("hog");

    Assertions.assertThrows(
      CliExitException.class,
      () -> command.run()
    );

    assertThat(testRenderer.getStdOut())
      .isEmpty();
    assertThat(testRenderer.getStdErr())
      .contains("Could not find group\n");
  }
}
