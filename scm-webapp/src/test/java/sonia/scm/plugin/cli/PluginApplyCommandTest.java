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
class PluginApplyCommandTest {

  @Mock
  private PluginTemplateRenderer templateRenderer;
  @Mock
  private PluginManager manager;

  @InjectMocks
  private PluginApplyCommand command;

  @Test
  void shouldRestartServer() {
    command.setRestart(true);
    PendingPlugins pendingPlugins = mock(PendingPlugins.class);
    when(manager.getPending()).thenReturn(pendingPlugins);
    when(pendingPlugins.existPendingChanges()).thenReturn(true);

    command.run();

    verify(manager).executePendingAndRestart();
    verify(templateRenderer).renderServerRestartTriggered();
  }

  @Test
  void shouldNotRestartServerIfFlagIsMissing() {
    command.run();

    verify(manager, never()).executePendingAndRestart();
    verify(templateRenderer).renderConfirmServerRestart();
  }

  @Test
  void shouldNotRestartServerIfNoPendingChanges() {
    command.setRestart(true);
    PendingPlugins pendingPlugins = mock(PendingPlugins.class);
    when(manager.getPending()).thenReturn(pendingPlugins);
    when(pendingPlugins.existPendingChanges()).thenReturn(false);

    command.run();

    verify(manager, never()).executePendingAndRestart();
    verify(templateRenderer).renderSkipServerRestart();
  }
}
