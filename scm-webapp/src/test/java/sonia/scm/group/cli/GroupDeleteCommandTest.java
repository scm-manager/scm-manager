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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.cli.TemplateTestRenderer;
import sonia.scm.group.Group;
import sonia.scm.group.GroupManager;

import java.util.ResourceBundle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupDeleteCommandTest {

  @Mock
  private GroupManager manager;

  private final TemplateTestRenderer testRenderer = new TemplateTestRenderer();
  private final GroupCommandBeanMapper mapper = new GroupCommandBeanMapperImpl();
  private final GroupTemplateRenderer templateRenderer = new GroupTemplateRenderer(testRenderer.getContextMock(), testRenderer.getTemplateEngineFactory(), mapper) {
    @Override
    protected ResourceBundle getBundle() {
      return testRenderer.getResourceBundle();
    }
  };

  private GroupDeleteCommand command;

  @BeforeEach
  void initCommand() {
    command = new GroupDeleteCommand(templateRenderer, manager);
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
