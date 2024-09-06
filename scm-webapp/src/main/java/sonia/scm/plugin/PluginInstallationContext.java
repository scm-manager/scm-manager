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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class PluginInstallationContext {

  private final Map<String, NameAndVersion> dependencies;

  private PluginInstallationContext(Map<String, NameAndVersion> dependencies) {
    this.dependencies = dependencies;
  }

  public static PluginInstallationContext empty() {
    return new PluginInstallationContext(Collections.emptyMap());
  }

  public static PluginInstallationContext fromDescriptors(Iterable<? extends PluginDescriptor> installed, Iterable<? extends PluginDescriptor> pending) {
    Map<String, NameAndVersion> dependencies = new HashMap<>();
    appendDescriptors(dependencies, installed);
    appendDescriptors(dependencies, pending);
    return new PluginInstallationContext(dependencies);
  }

  public static PluginInstallationContext from(Iterable<? extends Plugin> installed, Iterable<? extends Plugin> pending) {
    Map<String, NameAndVersion> dependencies = new HashMap<>();
    appendPlugins(dependencies, installed);
    appendPlugins(dependencies, pending);
    return new PluginInstallationContext(dependencies);
  }

  private static <P extends PluginDescriptor> void appendDescriptors(Map<String, NameAndVersion> dependencies, Iterable<P> descriptors) {
    descriptors.forEach(desc -> appendPlugins(dependencies, desc.getInformation()));
  }

  private static <P extends Plugin> void appendPlugins(Map<String, NameAndVersion> dependencies, Iterable<P> plugins) {
    plugins.forEach(plugin -> appendPlugins(dependencies, plugin.getDescriptor().getInformation()));
  }

  private static void appendPlugins(Map<String, NameAndVersion> dependencies, PluginInformation information) {
    dependencies.put(information.getName(), new NameAndVersion(information.getName(), information.getVersion()));
  }

  public Optional<NameAndVersion> find(String name) {
    return Optional.ofNullable(dependencies.get(name));
  }
}
