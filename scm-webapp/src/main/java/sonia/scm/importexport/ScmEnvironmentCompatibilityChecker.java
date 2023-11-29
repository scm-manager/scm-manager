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
