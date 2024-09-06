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
