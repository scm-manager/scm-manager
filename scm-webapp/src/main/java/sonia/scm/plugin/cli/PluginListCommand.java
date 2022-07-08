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
import sonia.scm.cli.Table;
import sonia.scm.cli.TemplateRenderer;
import sonia.scm.plugin.Plugin;
import sonia.scm.plugin.PluginManager;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
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
    Collection<Plugin> plugins = new ArrayList<>(manager.getInstalled());
    if (useShortTemplate) {
      templateRenderer.renderToStdout(SHORT_TEMPLATE, Map.of("plugins", plugins.stream().map(p -> p.getDescriptor().getInformation()).collect(Collectors.toList())));
    } else {
      Table table = templateRenderer.createTable();
      table.addHeader("scm.plugin.name", "scm.plugin.displayName", "scm.plugin.version", "scm.plugin.description");
      for (Plugin plugin : plugins) {
        table.addRow(
          plugin.getDescriptor().getInformation().getName(),
          plugin.getDescriptor().getInformation().getDisplayName(),
          plugin.getDescriptor().getInformation().getVersion(),
          plugin.getDescriptor().getInformation().getDescription()
        );
      }
      templateRenderer.renderToStdout(TABLE_TEMPLATE, Map.of("rows", table, "plugins", plugins));
    }
  }
}
