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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.plugin.PluginManager;
import sonia.scm.plugin.PluginTestHelper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PluginAddCommandTest {

  @Mock
  private PluginTemplateRenderer templateRenderer;
  @Mock
  private PluginManager manager;

  @InjectMocks
  private PluginAddCommand command;

  @Test
  void shouldAddPlugin() {
    String pluginName = "scm-test-plugin";
    doReturn(Optional.empty()).when(manager).getInstalled(pluginName);
    doReturn(Optional.of(PluginTestHelper.createAvailable(pluginName))).when(manager).getAvailable(pluginName);

    command.setName(pluginName);
    command.run();

    verify(manager).install(pluginName, false);
    verify(templateRenderer).renderPluginAdded(pluginName);
    verify(templateRenderer).renderServerRestartRequired();
  }

  @Test
  void shouldAddPluginWithRestart() {
    String pluginName = "scm-test-plugin";
    doReturn(Optional.empty()).when(manager).getInstalled(pluginName);
    doReturn(Optional.of(PluginTestHelper.createAvailable(pluginName))).when(manager).getAvailable(pluginName);

    command.setName(pluginName);
    command.setApply(true);
    command.run();

    verify(manager).install(pluginName, true);
    verify(templateRenderer).renderPluginAdded(pluginName);
    verify(templateRenderer).renderServerRestartTriggered();
  }

  @Test
  void shouldRenderErrorIfPluginAlreadyInstalled() {
    String pluginName = "scm-test-plugin";
    doReturn(Optional.of(PluginTestHelper.createInstalled(pluginName))).when(manager).getInstalled(pluginName);

    command.setName(pluginName);
    command.run();

    verify(templateRenderer).renderPluginAlreadyInstalledError();
  }

  @Test
  void shouldRenderErrorIfPluginNotAvailable() {
    String pluginName = "scm-test-plugin";
    doReturn(Optional.empty()).when(manager).getInstalled(pluginName);
    doReturn(Optional.empty()).when(manager).getAvailable(pluginName);

    command.setName(pluginName);
    command.run();

    verify(templateRenderer).renderPluginNotAvailableError();
  }

  @Test
  void shouldRenderErrorIfPluginInstallationFailed() {
    String pluginName = "scm-test-plugin";
    doReturn(Optional.empty()).when(manager).getInstalled(pluginName);
    doReturn(Optional.of(PluginTestHelper.createAvailable(pluginName))).when(manager).getAvailable(pluginName);
    doThrow(RuntimeException.class).when(manager).install(pluginName, false);

    command.setName(pluginName);
    assertThrows(RuntimeException.class, () -> command.run());

    verify(templateRenderer).renderPluginCouldNotBeAdded(pluginName);
  }

}
