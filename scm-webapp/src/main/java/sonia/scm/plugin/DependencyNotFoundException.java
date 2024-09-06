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

package sonia.scm.plugin;

import static sonia.scm.ContextEntry.ContextBuilder.entity;

@SuppressWarnings("squid:MaximumInheritanceDepth") // exceptions have a deep inheritance depth themselves; therefore we accept this here
public class DependencyNotFoundException extends PluginInstallException {

  private final String plugin;
  private final String missingDependency;

  public DependencyNotFoundException(String plugin, String missingDependency) {
    super(
      entity("Dependency", missingDependency)
        .in("Plugin", plugin)
        .build(),
      String.format(
        "missing dependency %s of plugin %s",
        missingDependency,
        plugin
      )
    );
    this.plugin = plugin;
    this.missingDependency = missingDependency;
  }

  public String getPlugin() {
    return plugin;
  }

  public String getMissingDependency() {
    return missingDependency;
  }

  @Override
  public String getCode() {
    return "5GS6lwvWF1";
  }
}
