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

import picocli.CommandLine;
import sonia.scm.cli.ParentCommand;
import sonia.scm.plugin.PluginManager;

import javax.inject.Inject;

@ParentCommand(value = PluginCommand.class)
@CommandLine.Command(name = "cancel-pending", aliases = "reset")
class PluginResetChangesCommand implements Runnable {

 @CommandLine.Mixin
  private final PluginTemplateRenderer templateRenderer;
  private final PluginManager manager;
  @CommandLine.Spec
  private CommandLine.Model.CommandSpec spec;

  @Inject
  PluginResetChangesCommand(PluginTemplateRenderer templateRenderer, PluginManager manager) {
    this.templateRenderer = templateRenderer;
    this.manager = manager;
  }

  @Override
  public void run() {
    if (manager.getPending().existPendingChanges()) {
      manager.cancelPending();
      templateRenderer.renderPluginsReseted();
    } else {
      templateRenderer.renderNoPendingPlugins();
    }
  }
}
