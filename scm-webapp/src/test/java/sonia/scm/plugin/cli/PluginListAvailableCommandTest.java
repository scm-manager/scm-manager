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
