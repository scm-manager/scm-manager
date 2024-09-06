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
import sonia.scm.plugin.PendingPlugins;
import sonia.scm.plugin.PluginManager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PluginResetChangesCommandTest {

  @Mock
  private PluginTemplateRenderer templateRenderer;
  @Mock
  private PluginManager manager;

  @InjectMocks
  private PluginResetChangesCommand command;

  @Test
  void shouldCancelPendingPlugins() {
    PendingPlugins pendingPlugins = mock(PendingPlugins.class);
    when(manager.getPending()).thenReturn(pendingPlugins);
    when(pendingPlugins.existPendingChanges()).thenReturn(true);

    command.run();

    verify(manager).cancelPending();
    verify(templateRenderer).renderPluginsReseted();
  }

  @Test
  void shouldRenderErrorIfNoPendingPlugins() {
    PendingPlugins pendingPlugins = mock(PendingPlugins.class);
    when(manager.getPending()).thenReturn(pendingPlugins);
    when(pendingPlugins.existPendingChanges()).thenReturn(false);

    command.run();

    verify(manager, never()).cancelPending();
    verify(templateRenderer).renderNoPendingPlugins();
  }
}
