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
import lombok.Getter;
import lombok.Setter;
import picocli.CommandLine;
import sonia.scm.cli.ParentCommand;
import sonia.scm.cli.Table;
import sonia.scm.cli.TemplateRenderer;
import sonia.scm.plugin.AvailablePlugin;
import sonia.scm.plugin.InstalledPlugin;
import sonia.scm.plugin.PendingPlugins;
import sonia.scm.plugin.PluginDescriptor;
import sonia.scm.plugin.PluginManager;

import javax.inject.Inject;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@ParentCommand(value = PluginCommand.class)
@CommandLine.Command(name = "list", aliases = "ls")
class PluginListCommand implements Runnable {

  @CommandLine.Mixin
  private final TemplateRenderer templateRenderer;
  private final PluginManager manager;
  @CommandLine.Spec
  private CommandLine.Model.CommandSpec spec;

  @CommandLine.Option(names = {"--short", "-s"})
  private boolean useShortTemplate;

  private static final String TABLE_TEMPLATE = String.join("\n",
    "{{#rows}}",
    "{{#cols}}{{#row.first}}{{#upper}}{{value}}{{/upper}}{{/row.first}}{{^row.first}}{{value}}{{/row.first}}{{^last}} {{/last}}{{/cols}}",
    "{{/rows}}"
  );

  private static final String SHORT_TEMPLATE = String.join("\n",
    "{{#plugins}}",
    "{{name}}",
    "{{/plugins}}"
  );

  @Inject
  public PluginListCommand(TemplateRenderer templateRenderer, PluginManager manager) {
    this.templateRenderer = templateRenderer;
    this.manager = manager;
  }

  @Override
  public void run() {
    Collection<ListablePlugin> plugins = getListablePlugins();
    if (useShortTemplate) {
      templateRenderer.renderToStdout(SHORT_TEMPLATE, Map.of("plugins", plugins));
    } else {
      Table table = templateRenderer.createTable();
      String yes = spec.resourceBundle().getString("yes");
      table.addHeader("scm.plugin.name", "scm.plugin.displayName", "scm.plugin.availableVersion", "scm.plugin.installedVersion", "scm.plugin.pending");

      for (ListablePlugin plugin : plugins) {
        table.addRow(
          plugin.getName(),
          plugin.getDisplayName(),
          plugin.getAvailableVersion(),
          plugin.getInstalledVersion(),
          plugin.isPending() ? yes : ""
        );
      }
      templateRenderer.renderToStdout(TABLE_TEMPLATE, Map.of("rows", table, "plugins", plugins));
    }
  }

  private List<ListablePlugin> getListablePlugins() {
    List<InstalledPlugin> installedPlugins = manager.getInstalled();
    List<AvailablePlugin> availablePlugins = manager.getAvailable();
    PendingPlugins pendingPlugins = manager.getPending();

    Set<ListablePlugin> plugins = new HashSet<>();
    for (PluginDescriptor pluginDesc : installedPlugins.stream().map(InstalledPlugin::getDescriptor).collect(Collectors.toList())) {
      ListablePlugin listablePlugin = new ListablePlugin(pendingPlugins, pluginDesc, true);
      setAvailableVersion(listablePlugin);
      plugins.add(listablePlugin);
    }

    for (PluginDescriptor pluginDesc : availablePlugins.stream().map(AvailablePlugin::getDescriptor).collect(Collectors.toList())) {
      if (plugins.stream().noneMatch(p -> p.name.equals(pluginDesc.getInformation().getName()))) {
        plugins.add(new ListablePlugin(pendingPlugins, pluginDesc, false));
      }
    }

    return plugins.stream().sorted((a, b) -> a.name.compareToIgnoreCase(b.name)).collect(Collectors.toList());
  }

  private void setAvailableVersion(ListablePlugin listablePlugin) {
    Optional<AvailablePlugin> availablePlugin = manager.getAvailable().stream().filter(p -> p.getDescriptor().getInformation().getName().equals(listablePlugin.name)).findFirst();
    if (availablePlugin.isPresent() && !availablePlugin.get().getDescriptor().getInformation().getVersion().equals(listablePlugin.installedVersion)) {
      listablePlugin.setAvailableVersion(availablePlugin.get().getDescriptor().getInformation().getVersion());
    }
  }

  @VisibleForTesting
  void setSpec(CommandLine.Model.CommandSpec spec) {
    this.spec = spec;
  }

  @VisibleForTesting
  void setUseShortTemplate(boolean useShortTemplate) {
    this.useShortTemplate = useShortTemplate;
  }

  @Getter
  @Setter
  static class ListablePlugin {
    private String name;
    private String displayName;
    private String installedVersion;
    private String availableVersion;
    private boolean pending;
    private boolean installed;

    ListablePlugin(PendingPlugins pendingPlugins, PluginDescriptor descriptor, boolean installed) {
      this.name = descriptor.getInformation().getName();
      this.displayName = descriptor.getInformation().getDisplayName();
      if (installed) {
        this.installedVersion = descriptor.getInformation().getVersion();
      } else {
        this.availableVersion = descriptor.getInformation().getVersion();
      }
      this.pending = pendingPlugins.isPending(descriptor.getInformation().getName());
      this.installed = installed;
    }
  }
}
