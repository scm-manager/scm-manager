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

package sonia.scm.plugin;

import com.google.common.annotations.VisibleForTesting;
import lombok.Value;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The plugin manager is responsible for plugin related tasks, such as install, uninstall or updating.
 *
 * @author Sebastian Sdorra
 */
public interface PluginManager {

  /**
   * Returns the available plugin with the given name.
   * @param name of plugin
   * @return optional available plugin.
   */
  Optional<AvailablePlugin> getAvailable(String name);

  /**
   * Returns the installed plugin with the given name.
   * @param name of plugin
   * @return optional installed plugin.
   */
  Optional<InstalledPlugin> getInstalled(String name);


  /**
   * Returns all installed plugins.
   *
   * @return a list of installed plugins.
   */
  List<InstalledPlugin> getInstalled();

  /**
   * @since 2.40.0
   */
  default PluginResult getPlugins() {
    return new PluginResult(getInstalled(), getAvailable(), false);
  }

  /**
   * Returns all available plugins. The list contains the plugins which are loaded from the plugin center, but without
   * the installed plugins.
   *
   * @return a list of available plugins.
   */
  List<AvailablePlugin> getAvailable();

  /**
   * Returns all available plugin sets from the plugin center.
   *
   * @return a list of available plugin sets
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
   *
   * @return a list of updatable plugins.
   */
  List<InstalledPlugin> getUpdatable();

  /**
   * Returns all pending plugins.
   *
   * @return a list of pending plugins.
   * @since 2.38.0
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
  class PluginResult {
    List<InstalledPlugin> installedPlugins;
    List<AvailablePlugin> availablePlugins;
    boolean pluginCenterError;

    public PluginResult(List<InstalledPlugin> installedPlugins, List<AvailablePlugin> availablePlugins, boolean pluginCenterError) {
      this.installedPlugins = installedPlugins;
      this.availablePlugins = availablePlugins;
      this.pluginCenterError = pluginCenterError;
    }

    @VisibleForTesting
    public PluginResult(List<InstalledPlugin> installedPlugins, List<AvailablePlugin> availablePlugins) {
      this(installedPlugins, availablePlugins, false);
    }
  }
}
