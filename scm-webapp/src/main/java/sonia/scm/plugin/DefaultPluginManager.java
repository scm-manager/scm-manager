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
import com.google.common.collect.ImmutableList;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.NotFoundException;
import sonia.scm.event.ScmEventBus;
import sonia.scm.lifecycle.Restarter;
import sonia.scm.version.Version;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.ScmConstraintViolationException.Builder.doThrow;

//~--- JDK imports ------------------------------------------------------------

/**
 * @author Sebastian Sdorra
 */
@Singleton
public class DefaultPluginManager implements PluginManager {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultPluginManager.class);

  private final PluginLoader loader;
  private final PluginCenter center;
  private final PluginInstaller installer;
  private final Restarter restarter;
  private final ScmEventBus eventBus;

  private final PluginSetConfigStore pluginSetConfigStore;

  private final Collection<PendingPluginInstallation> pendingInstallQueue = new ArrayList<>();
  private final Collection<PendingPluginUninstallation> pendingUninstallQueue = new ArrayList<>();
  private final PluginDependencyTracker dependencyTracker = new PluginDependencyTracker();

  private final Function<List<AvailablePlugin>, PluginInstallationContext> contextFactory;

  @Inject
  public DefaultPluginManager(PluginLoader loader, PluginCenter center, PluginInstaller installer, Restarter restarter, ScmEventBus eventBus, PluginSetConfigStore pluginSetConfigStore) {
    this(loader, center, installer, restarter, eventBus, null, pluginSetConfigStore);
  }

  DefaultPluginManager(PluginLoader loader, PluginCenter center, PluginInstaller installer, Restarter restarter, ScmEventBus eventBus, Function<List<AvailablePlugin>, PluginInstallationContext> contextFactory, PluginSetConfigStore pluginSetConfigStore) {
    this.loader = loader;
    this.center = center;
    this.installer = installer;
    this.restarter = restarter;
    this.eventBus = eventBus;
    this.pluginSetConfigStore = pluginSetConfigStore;

    if (contextFactory != null) {
      this.contextFactory = contextFactory;
    } else {
      this.contextFactory = (plugins -> {
        List<AvailablePlugin> pendingPlugins = new ArrayList<>(plugins);
        pendingInstallQueue.stream().map(PendingPluginInstallation::getPlugin).forEach(pendingPlugins::add);
        return PluginInstallationContext.from(getInstalled(), pendingPlugins);
      });
    }

    this.computeInstallationDependencies();
  }


  @VisibleForTesting
  synchronized void computeInstallationDependencies() {
    loader.getInstalledPlugins()
      .stream()
      .map(InstalledPlugin::getDescriptor)
      .forEach(dependencyTracker::addInstalled);
    updateMayUninstallFlag();
  }

  @Override
  public Optional<AvailablePlugin> getAvailable(String name) {
    PluginPermissions.read().check();
    return center.getAvailablePlugins()
      .stream()
      .filter(filterByName(name))
      .filter(this::isNotInstalledOrMoreUpToDate)
      .map(p -> getPending(name).orElse(p))
      .findFirst();
  }

  private Optional<AvailablePlugin> getPending(String name) {
    return pendingInstallQueue
      .stream()
      .map(PendingPluginInstallation::getPlugin)
      .filter(filterByName(name))
      .findFirst();
  }

  @Override
  public Optional<InstalledPlugin> getInstalled(String name) {
    PluginPermissions.read().check();
    return loader.getInstalledPlugins()
      .stream()
      .filter(filterByName(name))
      .findFirst();
  }

  @Override
  public List<InstalledPlugin> getInstalled() {
    PluginPermissions.read().check();
    return ImmutableList.copyOf(loader.getInstalledPlugins());
  }

  @Override
  public List<AvailablePlugin> getAvailable() {
    PluginPermissions.read().check();
    return center.getAvailablePlugins()
      .stream()
      .filter(this::isNotInstalledOrMoreUpToDate)
      .map(p -> getPending(p.getDescriptor().getInformation().getName()).orElse(p))
      .collect(Collectors.toList());
  }

  @Override
  public Set<PluginSet> getPluginSets() {
    PluginPermissions.read().check();
    return center.getAvailablePluginSets();
  }

  @Override
  public void installPluginSets(Set<String> pluginSetIds) {
    Set<PluginSet> pluginSets = getPluginSets();
    Set<PluginSet> pluginSetsToInstall = pluginSetIds.stream()
      .map(id -> pluginSets.stream().filter(pluginSet -> pluginSet.getId().equals(id)).findFirst())
      .filter(Optional::isPresent)
      .map(Optional::get)
      .collect(Collectors.toSet());

    Set<AvailablePlugin> pluginsToInstall = pluginSetsToInstall
      .stream()
      .map(pluginSet -> pluginSet
        .getPlugins()
        .stream()
        .map(this::collectPluginsToInstall).flatMap(Collection::stream).collect(Collectors.toSet())
      )
      .flatMap(Collection::stream)
      .collect(Collectors.toSet());

    Set<String> newlyIinstalledPluginSetIds = pluginSetsToInstall.stream().map(PluginSet::getId).collect(Collectors.toSet());

    Set<String> installedPluginSetIds = pluginSetConfigStore.getPluginSets().map(PluginSetsConfig::getPluginSets).orElse(new HashSet<>());
    installedPluginSetIds.addAll(newlyIinstalledPluginSetIds);
    pluginSetConfigStore.setPluginSets(new PluginSetsConfig(installedPluginSetIds));

    installPlugins(new ArrayList<>(pluginsToInstall), true);
  }

  @Override
  public List<InstalledPlugin> getUpdatable() {
    return getInstalled()
      .stream()
      .filter(p -> isUpdatable(p.getDescriptor().getInformation().getName()))
      .filter(p -> !p.isMarkedForUninstall())
      .collect(Collectors.toList());
  }

  private <T extends Plugin> Predicate<T> filterByName(String name) {
    return plugin -> name.equals(plugin.getDescriptor().getInformation().getName());
  }

  private boolean isNotInstalledOrMoreUpToDate(AvailablePlugin availablePlugin) {
    return getInstalled(availablePlugin.getDescriptor().getInformation().getName())
      .map(installedPlugin -> availableIsMoreUpToDateThanInstalled(availablePlugin, installedPlugin))
      .orElse(true);
  }

  private boolean availableIsMoreUpToDateThanInstalled(AvailablePlugin availablePlugin, InstalledPlugin installed) {
    return Version.parse(availablePlugin.getDescriptor().getInformation().getVersion()).isNewer(installed.getDescriptor().getInformation().getVersion());
  }

  @Override
  public void install(String name, boolean restartAfterInstallation) {
    PluginPermissions.write().check();

    getInstalled(name)
      .map(InstalledPlugin::isCore)
      .ifPresent(
        core -> doThrow().violation("plugin is a core plugin and cannot be updated").when(core)
      );

    List<AvailablePlugin> plugins = collectPluginsToInstall(name);
    installPlugins(plugins, restartAfterInstallation);
  }

  private void installPlugins(List<AvailablePlugin> plugins, boolean restartAfterInstallation) {
    List<PendingPluginInstallation> pendingInstallations = new ArrayList<>();

    for (AvailablePlugin plugin : plugins) {
      try {
        PendingPluginInstallation pending = installer.install(contextFactory.apply(plugins), plugin);
        dependencyTracker.addInstalled(plugin.getDescriptor());
        pendingInstallations.add(pending);
        eventBus.post(new PluginEvent(PluginEvent.PluginEventType.INSTALLED, plugin));
      } catch (PluginInstallException installException) {
        try {
          cancelPending(pendingInstallations);
        } catch (PluginFailedToCancelInstallationException cancelInstallationException) {
          LOG.error("could not install plugin {}; uninstallation failed (see next exception)", plugin.getDescriptor().getInformation().getName(), installException);
          throw cancelInstallationException;
        } finally {
          eventBus.post(new PluginEvent(PluginEvent.PluginEventType.INSTALLATION_FAILED, plugin));
        }
        throw installException;
      }
    }

    if (!pendingInstallations.isEmpty()) {
      if (restartAfterInstallation) {
        triggerRestart("plugin installation");
      } else {
        pendingInstallQueue.addAll(pendingInstallations);
        updateMayUninstallFlag();
      }
    }
  }

  @Override
  public void uninstall(String name, boolean restartAfterInstallation) {
    PluginPermissions.write().check();
    InstalledPlugin installed = getInstalled(name)
      .orElseThrow(() -> NotFoundException.notFound(entity(InstalledPlugin.class, name)));
    doThrow().violation("plugin is a core plugin and cannot be uninstalled").when(installed.isCore());

    markForUninstall(installed);

    if (restartAfterInstallation) {
      triggerRestart("plugin installation");
    } else {
      updateMayUninstallFlag();
    }
  }

  private void updateMayUninstallFlag() {
    loader.getInstalledPlugins()
      .forEach(p -> p.setUninstallable(isUninstallable(p)));
  }

  private boolean isUninstallable(InstalledPlugin p) {
    return !p.isCore()
      && !p.isMarkedForUninstall()
      && dependencyTracker.mayUninstall(p.getDescriptor().getInformation().getName());
  }

  private void markForUninstall(InstalledPlugin plugin) {
    dependencyTracker.removeInstalled(plugin.getDescriptor());
    try {
      Path file = Files.createFile(plugin.getDirectory().resolve(InstalledPlugin.UNINSTALL_MARKER_FILENAME));
      pendingUninstallQueue.add(new PendingPluginUninstallation(plugin, file));
      plugin.setMarkedForUninstall(true);
    } catch (Exception e) {
      dependencyTracker.addInstalled(plugin.getDescriptor());
      throw new PluginException("could not mark plugin " + plugin.getId() + " in path " + plugin.getDirectory() + "as " + InstalledPlugin.UNINSTALL_MARKER_FILENAME, e);
    }
  }

  @Override
  public void executePendingAndRestart() {
    PluginPermissions.write().check();
    if (!pendingInstallQueue.isEmpty() || getInstalled().stream().anyMatch(InstalledPlugin::isMarkedForUninstall)) {
      triggerRestart("execute pending plugin changes");
    }
  }

  private void triggerRestart(String cause) {
    restarter.restart(PluginManager.class, cause);
  }

  private void cancelPending(List<PendingPluginInstallation> pendingInstallations) {
    pendingInstallations.forEach(PendingPluginInstallation::cancel);
  }

  private List<AvailablePlugin> collectPluginsToInstall(String name) {
    List<AvailablePlugin> plugins = new ArrayList<>();
    collectPluginsToInstallOrUpdate(plugins, name);
    return plugins;
  }

  private void collectPluginsToInstallOrUpdate(List<AvailablePlugin> plugins, String name) {
    if (!isInstalledOrPending(name) || isUpdatable(name)) {
      collectDependentPlugins(plugins, name);
    } else {
      LOG.info("plugin {} is already installed or installation is pending, skipping installation", name);
    }
  }

  private void collectOptionalPluginToInstallOrUpdate(List<AvailablePlugin> plugins, String name) {
    if (isInstalledOrPending(name) && isUpdatable(name)) {
      collectDependentPlugins(plugins, name);
    } else {
      LOG.info("optional plugin {} is not installed or not updatable", name);
    }
  }

  private void collectDependentPlugins(List<AvailablePlugin> plugins, String name) {
    AvailablePlugin plugin = getAvailable(name).orElseThrow(() -> NotFoundException.notFound(entity(AvailablePlugin.class, name)));

    Set<String> dependencies = plugin.getDescriptor().getDependencies();
    if (dependencies != null) {
      for (String dependency : dependencies) {
        collectPluginsToInstallOrUpdate(plugins, dependency);
      }
    }

    Set<String> optionalDependencies = plugin.getDescriptor().getOptionalDependencies();
    if (optionalDependencies != null) {
      for (String optionalDependency : optionalDependencies) {
        collectOptionalPluginToInstallOrUpdate(plugins, optionalDependency);
      }
    }

    plugins.add(plugin);
  }

  private boolean isInstalledOrPending(String name) {
    return getInstalled(name).isPresent() || getPending(name).isPresent();
  }

  private boolean isUpdatable(String name) {
    return getAvailable(name).isPresent() && !getPending(name).isPresent();
  }

  @Override
  public void cancelPending() {
    PluginPermissions.write().check();
    pendingUninstallQueue.forEach(PendingPluginUninstallation::cancel);
    pendingInstallQueue.forEach(PendingPluginInstallation::cancel);
    pendingUninstallQueue.clear();
    pendingInstallQueue.clear();
    updateMayUninstallFlag();
  }

  @Override
  public void updateAll() {
    PluginPermissions.write().check();
    for (InstalledPlugin installedPlugin : getInstalled()) {
      String pluginName = installedPlugin.getDescriptor().getInformation().getName();
      if (isUpdatable(pluginName)) {
        install(pluginName, false);
      }
    }
  }
}
