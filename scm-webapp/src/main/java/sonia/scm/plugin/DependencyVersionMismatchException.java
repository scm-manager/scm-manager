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

import lombok.Getter;

import static sonia.scm.ContextEntry.ContextBuilder.entity;

@Getter
@SuppressWarnings("squid:MaximumInheritanceDepth") // exceptions have a deep inheritance depth themselves; therefore we accept this here
public class DependencyVersionMismatchException extends PluginInstallException {

  private final String plugin;
  private final String dependency;
  private final String minVersion;
  private final String currentVersion;

  public DependencyVersionMismatchException(String plugin, String dependency, String minVersion, String currentVersion) {
    super(
      entity("Dependency", dependency)
        .in("Plugin", plugin)
        .build(),
      String.format(
        "%s requires dependency %s at least in version %s, but it is installed in version %s",
        plugin, dependency, minVersion, currentVersion
      )
    );
    this.plugin = plugin;
    this.dependency = dependency;
    this.minVersion = minVersion;
    this.currentVersion = currentVersion;
  }

  @Override
  public String getCode() {
    return "E5S6niWwi1";
  }
}
