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

package sonia.scm.cli;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.InstalledPlugin;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.plugin.ScmModule;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

class RegisteredCommandCollector {

  private static final Logger LOG = LoggerFactory.getLogger(RegisteredCommandCollector.class);

  private final PluginLoader pluginLoader;

  @Inject
  public RegisteredCommandCollector(PluginLoader pluginLoader) {
    this.pluginLoader = pluginLoader;
  }

  public Set<RegisteredCommand> collect() {
    Set<RegisteredCommand> cmds = new HashSet<>();
    findCommands(pluginLoader.getUberClassLoader(), cmds, pluginLoader.getInstalledModules());
    findCommands(pluginLoader.getUberClassLoader(), cmds, pluginLoader.getInstalledPlugins().stream().map(InstalledPlugin::getDescriptor).collect(Collectors.toList()));
    return Collections.unmodifiableSet(cmds);
  }

  private void findCommands(ClassLoader classLoader, Set<RegisteredCommand> commands, Iterable<? extends ScmModule> modules) {
    modules.forEach(m -> m.getCliCommands().forEach(c -> {
      Class<?> command = createCommand(classLoader, c.getClazz());
      if (command != null && command != ScmManagerCommand.class) {
        commands.add(new RegisteredCommand(c.getName(), command, getParent(command)));
      }
    }));
  }


  private Class<?> getParent(Class<?> command) {
    ParentCommand parentAnnotation = command.getAnnotation(ParentCommand.class);
    if (parentAnnotation != null) {
      return parentAnnotation.value();
    }
    return null;
  }

  private Class<?> createCommand(ClassLoader classLoader, String clazz) {
    try {
      return classLoader.loadClass(clazz);
    } catch (ClassNotFoundException e) {
      LOG.error("Could not find command class: {}", clazz, e);
      return null;
    }
  }

}
