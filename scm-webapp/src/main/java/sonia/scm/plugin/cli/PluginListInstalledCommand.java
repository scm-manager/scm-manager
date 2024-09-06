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

import jakarta.inject.Inject;
import picocli.CommandLine;
import sonia.scm.cli.ParentCommand;
import sonia.scm.cli.TemplateRenderer;
import sonia.scm.plugin.PluginInformation;
import sonia.scm.plugin.PluginManager;

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
