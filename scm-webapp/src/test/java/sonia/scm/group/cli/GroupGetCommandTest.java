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
        "groupName:         hog",
        "groupDescription:  Crew of the Heart of Gold",
        "groupMembers:      zaphod, trillian",
        "groupExternal:     no",
        "groupCreationDate: 2022-04-06T16:20:00Z",
        "groupLastModified: 2022-04-08T23:53:20Z"
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
      .contains("Could not find group");
  }
}
