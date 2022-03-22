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

package sonia.scm.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.InstalledPlugin;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.plugin.ScmModule;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class CommandRegistry {

  private static final Logger LOG = LoggerFactory.getLogger(CommandRegistry.class);
  private final Set<Class<?>> commands;

  @Inject
  public CommandRegistry(PluginLoader pluginLoader) {
    Set<Class<?>> commands = new HashSet<>();
    findCommands(pluginLoader.getUberClassLoader(), commands, pluginLoader.getInstalledModules());
    findCommands(pluginLoader.getUberClassLoader(), commands, pluginLoader.getInstalledPlugins().stream().map(InstalledPlugin::getDescriptor).collect(Collectors.toList()));
    this.commands = Collections.unmodifiableSet(commands);
  }

  public Set<Class<?>> getCommands() {
    return commands;
  }

  private void findCommands(ClassLoader classLoader, Set<Class<?>> commands, Iterable<? extends ScmModule> modules) {
    modules.forEach(m -> m.getCliCommands().forEach(c -> {
      Class<?> command = createCommand(classLoader, c.getClazz());
      if (command != null && command != ScmManagerCommand.class) {
        commands.add(command);
      }
    }));
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
