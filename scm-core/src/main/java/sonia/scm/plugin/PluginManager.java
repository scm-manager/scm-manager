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

import com.google.common.annotations.VisibleForTesting;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The plugin manager is responsible for plugin related tasks, such as install, uninstall or updating.
 *
 */
public interface PluginManager {

  /**
   * Returns the available plugin with the given name.
   */
  Optional<AvailablePlugin> getAvailable(String name);

  /**
   * Returns the installed plugin with the given name.
   */
  Optional<InstalledPlugin> getInstalled(String name);


  /**
   * Returns all installed plugins.
   */
  List<InstalledPlugin> getInstalled();

  /**
   * @since 2.40.0
   */
  default PluginResult getPlugins() {
    return new PluginResult(getInstalled(), getAvailable());
  }

  /**
   * Returns all available plugins. The list contains the plugins which are loaded from the plugin center, but without
   * the installed plugins.
   */
  List<AvailablePlugin> getAvailable();

  /**
   * Returns all available plugin sets from the plugin center.
   * @since 2.35.0
   */
  Set<PluginSet> getPluginSets();

  /**
   * Collects and installs all plugins and their dependencies for the given plugin sets.
   *
   * @param pluginSets Ids of plugin sets to install
   * @param restartAfterInstallation restart context after all plugins have been installed
   * @since 2.35.0
   */
  void installPluginSets(Set<String> pluginSets, boolean restartAfterInstallation);

  /**
   * Returns all updatable plugins.
   */
  List<InstalledPlugin> getUpdatable();

  /**
   * Returns all pending plugins.
   *
   *  @since 2.38.0
   */
  PendingPlugins getPending();

  /**
   * Installs the plugin with the given name from the list of available plugins.
   *
   * @param name plugin name
   * @param restartAfterInstallation restart context after plugin installation
   */
  void install(String name, boolean restartAfterInstallation);

  /**
   * Marks the plugin with the given name for uninstall.
   *
   * @param name plugin name
   * @param restartAfterInstallation restart context after plugin has been marked to really uninstall the plugin
   */
  void uninstall(String name, boolean restartAfterInstallation);

  /**
   * Install all pending plugins and restart the scm context.
   */
  void executePendingAndRestart();

  /**
   * Cancel all pending plugins.
   */
  void cancelPending();

  /**
   * Update all installed plugins.
   */
  void updateAll();

  /**
   * Returned by {@link #getPlugins()}.
   * @since 2.40.0
   */
  @Value
  @AllArgsConstructor
  class PluginResult {
    List<InstalledPlugin> installedPlugins;
    List<AvailablePlugin> availablePlugins;
    PluginCenterStatus pluginCenterStatus;

    @VisibleForTesting
    public PluginResult(List<InstalledPlugin> installedPlugins, List<AvailablePlugin> availablePlugins) {
      this(installedPlugins, availablePlugins, PluginCenterStatus.OK);
    }

  }
}
