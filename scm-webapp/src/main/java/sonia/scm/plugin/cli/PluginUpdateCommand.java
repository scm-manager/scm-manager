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

import com.cronutils.utils.VisibleForTesting;
import picocli.CommandLine;
import sonia.scm.cli.ParentCommand;
import sonia.scm.plugin.PluginManager;

import javax.inject.Inject;
import java.util.Objects;

@ParentCommand(value = PluginCommand.class)
@CommandLine.Command(name = "update")
class PluginUpdateCommand implements Runnable {

  @CommandLine.Parameters(index = "0", paramLabel = "<name>", descriptionKey = "scm.plugin.name")
  private String name;

  @CommandLine.Option(names = {"--apply", "-a"}, descriptionKey = "scm.plugin.apply")
  private boolean apply;

  @CommandLine.Mixin
  private final PluginTemplateRenderer templateRenderer;
  private final PluginManager manager;
  @CommandLine.Spec
  private CommandLine.Model.CommandSpec spec;

  @Inject
    PluginUpdateCommand(PluginTemplateRenderer templateRenderer, PluginManager manager) {
    this.templateRenderer = templateRenderer;
    this.manager = manager;
  }

  @Override
  public void run() {
    if (manager.getInstalled(name).isEmpty()) {
      templateRenderer.renderPluginNotInstalledError();
      return;
    }
    if (manager.getUpdatable().stream().noneMatch(p -> Objects.equals(p.getDescriptor().getInformation().getName(), name))) {
      templateRenderer.renderPluginNotUpdatable(name);
      return;
    }
    manager.install(name, apply);
    templateRenderer.renderPluginUpdated(name);
    if (!apply) {
      templateRenderer.renderServerRestartRequired();
    } else {
      templateRenderer.renderServerRestartTriggered();
    }
  }
  @VisibleForTesting
  void setName(String name) {
    this.name = name;
  }
  @VisibleForTesting
  void setApply(boolean apply) {
    this.apply = apply;
  }
}
