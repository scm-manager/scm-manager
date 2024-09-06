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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PluginUpdateAllCommandTest {

  @Mock
  private PluginTemplateRenderer templateRenderer;
  @Mock
  private PluginManager pluginManager;

  @InjectMocks
  private PluginUpdateAllCommand command;

  @Test
  void shouldUpdateAll() {
    command.run();

    verify(pluginManager).updateAll();
    verify(templateRenderer).renderAllPluginsUpdated();
    verify(templateRenderer).renderServerRestartRequired();
  }

  @Test
  void shouldUpdateAllWithRestart() {
    command.setApply(true);

    command.run();

    verify(pluginManager).updateAll();
    verify(pluginManager).executePendingAndRestart();
    verify(templateRenderer).renderAllPluginsUpdated();
    verify(templateRenderer).renderServerRestartTriggered();
  }


  @Test
  void shouldRenderErrorIfUpdateFailed() {
    doThrow(RuntimeException.class).when(pluginManager).updateAll();

    assertThrows(RuntimeException.class, () -> command.run());

    verify(templateRenderer).renderPluginsUpdateError();
  }
}
