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

import com.cronutils.utils.VisibleForTesting;
import jakarta.inject.Inject;
import picocli.CommandLine;
import sonia.scm.cli.ParentCommand;
import sonia.scm.plugin.PluginManager;

@ParentCommand(value = PluginCommand.class)
@CommandLine.Command(name = "add")
class PluginAddCommand implements Runnable {

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
  PluginAddCommand(PluginTemplateRenderer templateRenderer, PluginManager manager) {
    this.templateRenderer = templateRenderer;
    this.manager = manager;
  }

  @Override
  public void run() {
    if (manager.getInstalled(name).isPresent()) {
      templateRenderer.renderPluginAlreadyInstalledError();
      return;
    }
    if (manager.getAvailable(name).isEmpty()) {
      templateRenderer.renderPluginNotAvailableError();
      return;
    }

    try {
      manager.install(name, apply);
    } catch (Exception e) {
      templateRenderer.renderPluginCouldNotBeAdded(name);
      throw e;
    }
    templateRenderer.renderPluginAdded(name);
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
