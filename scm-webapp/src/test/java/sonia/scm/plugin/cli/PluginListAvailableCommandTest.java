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

package sonia.scm.plugin.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.cli.TemplateTestRenderer;
import sonia.scm.plugin.PendingPlugins;
import sonia.scm.plugin.PluginManager;
import sonia.scm.plugin.PluginTestHelper;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class PluginListAvailableCommandTest {

  private final TemplateTestRenderer templateTestRenderer = new TemplateTestRenderer();
  @Mock
  private PluginManager manager;

  private PluginListAvailableCommand command;

  @BeforeEach
  void initCommand() {
    command = new PluginListAvailableCommand(templateTestRenderer.createTemplateRenderer(), manager);
    command.setSpec(templateTestRenderer.getMockedSpec());
    PendingPlugins pendingPlugins = mock(PendingPlugins.class);
    lenient().doReturn(pendingPlugins).when(manager).getPending();
  }

  @Test
  void shouldListPlugins() {
    doReturn(of(
      PluginTestHelper.createAvailable("scm-review-plugin"),
      PluginTestHelper.createAvailable("scm-test-plugin", "1.1.0"))
    ).when(manager).getAvailable();

    command.run();

    assertThat(templateTestRenderer.getStdOut())
      .contains("NAME              DISPLAY NAME AVAILABLE PENDING?")
      .contains("scm-review-plugin              1.0")
      .contains("scm-test-plugin                1.1.0");
  }

  @Test
  void shouldListPluginsAsShortList() {
    doReturn(of(
      PluginTestHelper.createAvailable("scm-review-plugin"),
      PluginTestHelper.createAvailable("scm-test-plugin", "1.1.0"),
      PluginTestHelper.createAvailable("scm-archive-plugin"))
    ).when(manager).getAvailable();
    command.setUseShortTemplate(true);

    command.run();

    assertThat(templateTestRenderer.getStdOut())
      .isEqualTo("scm-archive-plugin\nscm-review-plugin\nscm-test-plugin\n");
  }
}
