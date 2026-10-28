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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static sonia.scm.ScmConstraintViolationException.Builder.doThrow;

class PluginDependencyTracker {

  private final Map<String, Collection<String>> plugins = new HashMap<>();

  void addInstalled(PluginDescriptor plugin) {
    if (plugin.getDependencies() != null) {
      plugin.getDependencies().forEach(dependency -> addDependency(plugin.getInformation().getName(), dependency));
    }
  }

  void removeInstalled(PluginDescriptor plugin) {
    doThrow()
      .violation("Plugin is needed as a dependency for other plugins", "plugin")
      .when(!mayUninstall(plugin.getInformation().getName()));
    if (plugin.getDependencies() != null) {
      plugin.getDependencies().forEach(dependency -> removeDependency(plugin.getInformation().getName(), dependency));
    }
  }

  List<InstalledPlugin> orderForUninstall(Collection<InstalledPlugin> selected) {
    PluginDependencyTracker remaining = new PluginDependencyTracker();
    plugins.forEach((name, dependents) -> remaining.plugins.put(name, new HashSet<>(dependents)));
    List<InstalledPlugin> pending = new ArrayList<>(selected);
    List<InstalledPlugin> ordered = new ArrayList<>();
    while (!pending.isEmpty()) {
      List<InstalledPlugin> removable = pending.stream()
        .filter(plugin -> remaining.mayUninstall(plugin.getDescriptor().getInformation().getName()))
        .toList();
      doThrow()
        .violation("Plugins are needed as dependencies by other plugins or have cyclic dependencies", "plugin")
        .when(removable.isEmpty());
      removable.forEach(plugin -> remaining.removeInstalled(plugin.getDescriptor()));
      ordered.addAll(removable);
      pending.removeAll(removable);
    }
    return ordered;
  }

  boolean mayUninstall(String name) {
    return plugins.computeIfAbsent(name, x -> new HashSet<>()).isEmpty();
  }

  private void addDependency(String from, String to) {
    plugins.computeIfAbsent(to, name -> new HashSet<>()).add(from);
  }

  private void removeDependency(String from, String to) {
    Collection<String> dependencies = plugins.get(to);
    if (dependencies == null) {
      throw new NullPointerException("inverse dependencies not found for " + to);
    }
    dependencies.remove(from);
  }
}
