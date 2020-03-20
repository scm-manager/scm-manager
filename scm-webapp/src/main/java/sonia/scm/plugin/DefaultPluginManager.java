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
import sonia.scm.lifecycle.RestartEvent;
import sonia.scm.version.Version;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.ScmConstraintViolationException.Builder.doThrow;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class DefaultPluginManager implements PluginManager {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultPluginManager.class);

  private final ScmEventBus eventBus;
  private final PluginLoader loader;
  private final PluginCenter center;
  private final PluginInstaller installer;
  private final Collection<PendingPluginInstallation> pendingInstallQueue = new ArrayList<>();
  private final Collection<PendingPluginUninstallation> pendingUninstallQueue = new ArrayList<>();
  private final PluginDependencyTracker dependencyTracker = new PluginDependencyTracker();

  @Inject
  public DefaultPluginManager(ScmEventBus eventBus, PluginLoader loader, PluginCenter center, PluginInstaller installer) {
    this.eventBus = eventBus;
    this.loader = loader;
    this.center = center;
    this.installer = installer;

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
    return center.getAvailable()
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
    return center.getAvailable()
      .stream()
      .filter(this::isNotInstalledOrMoreUpToDate)
      .map(p -> getPending(p.getDescriptor().getInformation().getName()).orElse(p))
      .collect(Collectors.toList());
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
    PluginPermissions.manage().check();

    getInstalled(name)
      .map(InstalledPlugin::isCore)
      .ifPresent(
        core -> doThrow().violation("plugin is a core plugin and cannot be updated").when(core)
      );

    List<AvailablePlugin> plugins = collectPluginsToInstall(name);
    List<PendingPluginInstallation> pendingInstallations = new ArrayList<>();
    for (AvailablePlugin plugin : plugins) {
      try {
        PendingPluginInstallation pending = installer.install(plugin);
        dependencyTracker.addInstalled(plugin.getDescriptor());
        pendingInstallations.add(pending);
      } catch (PluginInstallException ex) {
        cancelPending(pendingInstallations);
        throw ex;
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
    PluginPermissions.manage().check();
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
    PluginPermissions.manage().check();
    if (!pendingInstallQueue.isEmpty() || getInstalled().stream().anyMatch(InstalledPlugin::isMarkedForUninstall)) {
      triggerRestart("execute pending plugin changes");
    }
  }

  @VisibleForTesting
  void triggerRestart(String cause) {
    new Thread(() -> {
      try {
        Thread.sleep(200);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      eventBus.post(new RestartEvent(PluginManager.class, cause));
    }).start();
  }

  private void cancelPending(List<PendingPluginInstallation> pendingInstallations) {
    pendingInstallations.forEach(PendingPluginInstallation::cancel);
  }

  private List<AvailablePlugin> collectPluginsToInstall(String name) {
    List<AvailablePlugin> plugins = new ArrayList<>();
    collectPluginsToInstall(plugins, name, true);
    return plugins;
  }

  private void collectPluginsToInstall(List<AvailablePlugin> plugins, String name, boolean isUpdate) {
    if (!isInstalledOrPending(name) || isUpdate && isUpdatable(name)) {
      AvailablePlugin plugin = getAvailable(name).orElseThrow(() -> NotFoundException.notFound(entity(AvailablePlugin.class, name)));

      Set<String> dependencies = plugin.getDescriptor().getDependencies();
      if (dependencies != null) {
        for (String dependency: dependencies){
          collectPluginsToInstall(plugins, dependency, false);
        }
      }

      plugins.add(plugin);
    } else {
      LOG.info("plugin {} is already installed or installation is pending, skipping installation", name);
    }
  }

  private boolean isInstalledOrPending(String name) {
    return getInstalled(name).isPresent() || getPending(name).isPresent();
  }

  private boolean isUpdatable(String name) {
    return getAvailable(name).isPresent() && !getPending(name).isPresent();
  }

  @Override
  public void cancelPending() {
    PluginPermissions.manage().check();
    pendingUninstallQueue.forEach(PendingPluginUninstallation::cancel);
    pendingInstallQueue.forEach(PendingPluginInstallation::cancel);
    pendingUninstallQueue.clear();
    pendingInstallQueue.clear();
    updateMayUninstallFlag();
  }

  @Override
  public void updateAll() {
    PluginPermissions.manage().check();
    for (InstalledPlugin installedPlugin : getInstalled()) {
      String pluginName = installedPlugin.getDescriptor().getInformation().getName();
      if (isUpdatable(pluginName)) {
        install(pluginName, false);
      }
    }
  }
}
