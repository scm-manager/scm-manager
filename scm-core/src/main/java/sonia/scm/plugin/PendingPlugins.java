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

import java.util.List;

import static com.google.common.collect.Iterables.contains;
import static java.util.stream.Collectors.toList;

public class PendingPlugins {

  private final List<AvailablePlugin> install;
  private final List<InstalledPlugin> update;
  private final List<InstalledPlugin> uninstall;

  public PendingPlugins(List<AvailablePlugin> availablePlugins, List<InstalledPlugin> installedPlugins) {
    List<AvailablePlugin> pending = availablePlugins
      .stream()
      .filter(AvailablePlugin::isPending)
      .collect(toList());

    this.install = pending
      .stream()
      .filter(a -> !contains(installedPlugins, a)).collect(toList());
    this.update = installedPlugins
      .stream()
      .filter(i -> contains(pending, i)).collect(toList());
    this.uninstall = installedPlugins
      .stream()
      .filter(InstalledPlugin::isMarkedForUninstall).collect(toList());
  }

  public List<AvailablePlugin> getInstall() {
    return install;
  }

  public List<InstalledPlugin> getUpdate() {
    return update;
  }

  public List<InstalledPlugin> getUninstall() {
    return uninstall;
  }

  public boolean isPending(String name) {
    return uninstall.stream().anyMatch(p -> p.getDescriptor().getInformation().getName().equals(name))
      || update.stream().anyMatch(p -> p.getDescriptor().getInformation().getName().equals(name))
      || install.stream().anyMatch(p -> p.getDescriptor().getInformation().getName().equals(name));
  }

  public boolean existPendingChanges() {
    return !uninstall.isEmpty() || !update.isEmpty() || !install.isEmpty();
  }
}
