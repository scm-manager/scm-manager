/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.ImmutableList;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.NotFoundException;

//~--- JDK imports ------------------------------------------------------------
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static sonia.scm.ContextEntry.ContextBuilder.entity;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class DefaultPluginManager implements PluginManager {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultPluginManager.class);

  private final PluginLoader loader;
  private final PluginCenter center;
  private final PluginInstaller installer;

  @Inject
  public DefaultPluginManager(PluginLoader loader, PluginCenter center, PluginInstaller installer) {
    this.loader = loader;
    this.center = center;
    this.installer = installer;
  }

  @Override
  public Optional<AvailablePlugin> getAvailable(String name) {
    return center.getAvailable()
      .stream()
      .filter(filterByName(name))
      .filter(this::isNotInstalled)
      .findFirst();
  }

  @Override
  public Optional<InstalledPlugin> getInstalled(String name) {
    return loader.getInstalledPlugins()
      .stream()
      .filter(filterByName(name))
      .findFirst();
  }

  @Override
  public List<InstalledPlugin> getInstalled() {
    return ImmutableList.copyOf(loader.getInstalledPlugins());
  }

  @Override
  public List<AvailablePlugin> getAvailable() {
    return center.getAvailable().stream().filter(this::isNotInstalled).collect(Collectors.toList());
  }

  private <T extends Plugin> Predicate<T> filterByName(String name) {
    return (plugin) -> name.equals(plugin.getDescriptor().getInformation().getName());
  }

  private boolean isNotInstalled(AvailablePlugin availablePlugin) {
    return !getInstalled(availablePlugin.getDescriptor().getInformation().getName()).isPresent();
  }

  @Override
  public void install(String name) {
    if (getInstalled(name).isPresent()){
      LOG.info("plugin {} is already installed, skipping installation", name);
      return;
    }
    AvailablePlugin plugin = getAvailable(name).orElseThrow(() -> NotFoundException.notFound(entity(AvailablePlugin.class, name)));
    Set<String> dependencies = plugin.getDescriptor().getDependencies();
    if (dependencies != null) {
      for (String dependency: dependencies){
        install(dependency);
      }
    }

    installer.install(plugin);
  }
}
