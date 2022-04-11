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
      .isEqualTo("Could not find group");
  }
}
