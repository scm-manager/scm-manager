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

import com.google.common.annotations.VisibleForTesting;
import picocli.CommandLine;
import sonia.scm.cli.Table;
import sonia.scm.cli.TemplateRenderer;
import sonia.scm.plugin.PendingPlugins;
import sonia.scm.plugin.PluginInformation;
import sonia.scm.plugin.PluginManager;

import java.util.Collection;
import java.util.Map;

import static sonia.scm.plugin.cli.PluginListCommand.SHORT_TEMPLATE;
import static sonia.scm.plugin.cli.PluginListCommand.TABLE_TEMPLATE;

abstract class PluginSingleListBaseCommand {

  @CommandLine.Mixin
  private final TemplateRenderer templateRenderer;
  private final PluginManager manager;

  @CommandLine.Spec
  private CommandLine.Model.CommandSpec spec;
  @CommandLine.Option(names = {"--short", "-s"}, descriptionKey = "scm.plugin.list.short")
  private boolean useShortTemplate;

  PluginSingleListBaseCommand(TemplateRenderer templateRenderer, PluginManager manager) {
    this.templateRenderer = templateRenderer;
    this.manager = manager;
  }

  void renderResult(Collection<PluginInformation> plugins, String[] header) {
    if (useShortTemplate) {
      templateRenderer.renderToStdout(SHORT_TEMPLATE, Map.of("plugins", plugins));
    } else {
      Table table = templateRenderer.createTable();
      String yes = spec.resourceBundle().getString("yes");
      table.addHeader(header);

      PendingPlugins pendingPlugins = manager.getPending();
      for (PluginInformation plugin : plugins) {
        table.addRow(
          plugin.getName(),
          plugin.getDisplayName(),
          plugin.getVersion(),
          pendingPlugins.isPending(plugin.getName()) ? yes : ""
        );
      }
      templateRenderer.renderToStdout(TABLE_TEMPLATE, Map.of("rows", table, "plugins", plugins));
    }
  }

  @VisibleForTesting
  void setUseShortTemplate(boolean useShortTemplate) {
    this.useShortTemplate = useShortTemplate;
  }

  @VisibleForTesting
  void setSpec(CommandLine.Model.CommandSpec spec) {
    this.spec = spec;
  }
}
