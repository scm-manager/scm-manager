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
import sonia.scm.cli.TemplateRenderer;
import sonia.scm.plugin.PluginInformation;
import sonia.scm.plugin.PluginManager;

import javax.inject.Inject;
import java.util.Collection;
import java.util.stream.Collectors;

@ParentCommand(value = PluginCommand.class)
@CommandLine.Command(name = "list-installed", aliases = "lsi")
public class PluginListInstalledCommand extends PluginSingleListBaseCommand implements Runnable {

  private final PluginManager manager;

  @Inject
  PluginListInstalledCommand(TemplateRenderer templateRenderer, PluginManager manager) {
    super(templateRenderer, manager);
    this.manager = manager;
  }

  @Override
  public void run() {
    Collection<PluginInformation> plugins = manager.getInstalled().stream()
      .map(p -> p.getDescriptor().getInformation())
      .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
      .collect(Collectors.toList());
    String[] header = {"scm.plugin.name", "scm.plugin.displayName", "scm.plugin.installedVersion", "scm.plugin.pending"};
    renderResult(plugins, header);
  }
}
