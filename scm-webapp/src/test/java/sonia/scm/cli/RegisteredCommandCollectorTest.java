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

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.plugin.InstalledPlugin;
import sonia.scm.plugin.InstalledPluginDescriptor;
import sonia.scm.plugin.NamedClassElement;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.plugin.ScmModule;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisteredCommandCollectorTest {

  @Mock
  private PluginLoader pluginLoader;

  @InjectMocks
  private RegisteredCommandCollector commandCollector;

  @Test
  void shouldCollectCommandsFromModulesAndPlugins() {
    ScmModule module = mock(ScmModule.class);
    when(pluginLoader.getInstalledModules()).thenReturn(ImmutableList.of(module));

    when(module.getCliCommands()).thenReturn(ImmutableList.of(new NamedClassElement("moduleCommand", ModuleCommand.class.getName())));
    InstalledPlugin installedPlugin = mock(InstalledPlugin.class);
    InstalledPluginDescriptor descriptor = mock(InstalledPluginDescriptor.class);
    when(pluginLoader.getInstalledPlugins()).thenReturn(ImmutableList.of(installedPlugin));
    when(installedPlugin.getDescriptor()).thenReturn(descriptor);
    when(descriptor.getCliCommands()).thenReturn(ImmutableList.of(new NamedClassElement("subCommand", SubCommand.class.getName())));
    when(pluginLoader.getUberClassLoader()).thenReturn(RegisteredCommandCollectorTest.class.getClassLoader());

    Set<RegisteredCommand> commands = commandCollector.collect();

    assertThat(commands).hasSize(2);
    assertThat(commands)
      .map(RegisteredCommand::getName)
      .containsExactlyInAnyOrder("subCommand", "moduleCommand");

    List<Class<?>> commandClasses = commands.stream().map(RegisteredCommand::getCommand).collect(Collectors.toList());
    assertThat(commandClasses).containsExactlyInAnyOrder(SubCommand.class, ModuleCommand.class);
  }

  static class ParentCommand {

  }

  @sonia.scm.cli.ParentCommand(value = ParentCommand.class)
  static class SubCommand {

  }

  @sonia.scm.cli.ParentCommand(value = ParentCommand.class)
  static class ModuleCommand {

  }
}
