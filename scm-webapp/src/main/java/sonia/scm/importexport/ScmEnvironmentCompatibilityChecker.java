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

package sonia.scm.importexport;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContextProvider;
import sonia.scm.plugin.PluginInformation;
import sonia.scm.plugin.PluginManager;
import sonia.scm.version.Version;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ScmEnvironmentCompatibilityChecker {

  private static final Logger LOG = LoggerFactory.getLogger(ScmEnvironmentCompatibilityChecker.class);
  private final PluginManager pluginManager;
  private final SCMContextProvider scmContextProvider;

  @Inject
  public ScmEnvironmentCompatibilityChecker(PluginManager pluginManager, SCMContextProvider scmContextProvider) {
    this.pluginManager = pluginManager;
    this.scmContextProvider = scmContextProvider;
  }

  boolean check(ScmEnvironment environment) {
    return isCoreVersionCompatible(scmContextProvider.getVersion(), environment.getCoreVersion())
      && arePluginsCompatible(environment);
  }

  private boolean isCoreVersionCompatible(String currentCoreVersion, String coreVersionFromImport) {
    boolean compatible = Version.parse(currentCoreVersion).isNewerOrEqual(coreVersionFromImport);
    if (!compatible) {
      LOG.info(
        "SCM-Manager version is not compatible with dump. Dump can only be imported with SCM-Manager version {} or newer; you are running version {}.",
        coreVersionFromImport,
        currentCoreVersion
      );
    }
    return compatible;
  }

  private boolean arePluginsCompatible(ScmEnvironment environment) {
    List<PluginInformation> currentlyInstalledPlugins = pluginManager.getInstalled()
      .stream()
      .map(p -> p.getDescriptor().getInformation())
      .collect(Collectors.toList());

    for (EnvironmentPluginDescriptor plugin : environment.getPlugins().getPlugin()) {
      Optional<PluginInformation> matchingInstalledPlugin = findMatchingInstalledPlugin(currentlyInstalledPlugins, plugin);
      if (matchingInstalledPlugin.isPresent() && isPluginIncompatible(plugin, matchingInstalledPlugin.get())) {
        LOG.info(
          "The installed plugin \"{}\" with version \"{}\" is older than the plugin data version \"{}\" from the SCM-Manager environment the dump was created with. Please update the plugin.",
          matchingInstalledPlugin.get().getName(),
          matchingInstalledPlugin.get().getVersion(),
          plugin.getVersion()
        );
        return false;
      }
    }
    return true;
  }

  private boolean isPluginIncompatible(EnvironmentPluginDescriptor plugin, PluginInformation matchingInstalledPlugin) {
    return isPluginVersionIncompatible(plugin.getVersion(), matchingInstalledPlugin.getVersion());
  }

  private Optional<PluginInformation> findMatchingInstalledPlugin(List<PluginInformation> currentlyInstalledPlugins, EnvironmentPluginDescriptor plugin) {
    return currentlyInstalledPlugins
      .stream()
      .filter(p -> p.getName().equalsIgnoreCase(plugin.getName()))
      .findFirst();
  }

  private boolean isPluginVersionIncompatible(String previousPluginVersion, String installedPluginVersion) {
    return Version.parse(installedPluginVersion).isOlder(previousPluginVersion);
  }
}
