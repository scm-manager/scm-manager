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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PluginUpdateCommandTest {


  @Mock
  private PluginTemplateRenderer templateRenderer;
  @Mock
  private PluginManager manager;

  @InjectMocks
  private PluginUpdateCommand command;

  @Test
  void shouldUpdateSinglePlugin() {
    String pluginName = "scm-test-plugin";
    doReturn(Optional.of(PluginTestHelper.createInstalled(pluginName))).when(manager).getInstalled(pluginName);
    doReturn(singletonList(PluginTestHelper.createInstalled(pluginName))).when(manager).getUpdatable();

    command.setName(pluginName);

    command.run();

    verify(manager).install(pluginName, false);
    verify(templateRenderer).renderPluginUpdated(pluginName);
    verify(templateRenderer).renderServerRestartRequired();
  }

  @Test
  void shouldUpdateSinglePluginWithRestart() {
    String pluginName = "scm-test-plugin";
    doReturn(Optional.of(PluginTestHelper.createInstalled(pluginName))).when(manager).getInstalled(pluginName);
    doReturn(singletonList(PluginTestHelper.createInstalled(pluginName))).when(manager).getUpdatable();

    command.setName(pluginName);
    command.setApply(true);

    command.run();

    verify(manager).install(pluginName, true);
    verify(templateRenderer).renderPluginUpdated(pluginName);
    verify(templateRenderer).renderServerRestartTriggered();
  }

  @Test
  void shouldRenderErrorIfPluginNotInstalled() {
    String pluginName = "scm-test-plugin";
    doReturn(Optional.empty()).when(manager).getInstalled(pluginName);

    command.setName(pluginName);

    command.run();

    verify(manager, never()).install(eq(pluginName), anyBoolean());
    verify(templateRenderer).renderPluginNotInstalledError();
  }

  @Test
  void shouldRenderErrorIfPluginNotUpdatable() {
    String pluginName = "scm-test-plugin";
    doReturn(Optional.of(PluginTestHelper.createInstalled(pluginName))).when(manager).getInstalled(pluginName);
    doReturn(emptyList()).when(manager).getUpdatable();

    command.setName(pluginName);

    command.run();

    verify(manager, never()).install(eq(pluginName), anyBoolean());
    verify(templateRenderer).renderPluginNotUpdatable(pluginName);
  }
}
