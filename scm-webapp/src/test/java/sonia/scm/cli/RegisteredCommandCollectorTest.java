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
