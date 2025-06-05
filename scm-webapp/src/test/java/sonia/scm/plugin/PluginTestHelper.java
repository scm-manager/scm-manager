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

import org.mockito.Answers;

import static org.mockito.Mockito.*;

public class PluginTestHelper {
  public static AvailablePlugin createAvailable(String name) {
    return createAvailable(name, "1.0");
  }

  public static AvailablePlugin createAvailable(String name, String version) {
    PluginInformation information = new PluginInformation();
    information.setName(name);
    information.setVersion(version);
    return createAvailable(information);
  }

  public static InstalledPlugin createInstalled(String name) {
    return createInstalled(name, "1.0");
  }

  public static InstalledPlugin createInstalled(String name, String version) {
    PluginInformation information = new PluginInformation();
    information.setName(name);
    information.setVersion(version);
    return createInstalled(information);
  }

  public static InstalledPlugin createInstalled(PluginInformation information) {
    InstalledPlugin plugin = mock(InstalledPlugin.class, Answers.RETURNS_DEEP_STUBS);
    returnInformation(plugin, information);
    return plugin;
  }

  public static AvailablePlugin createAvailable(PluginInformation information) {
    AvailablePluginDescriptor descriptor = mock(AvailablePluginDescriptor.class);
    lenient().when(descriptor.getInformation()).thenReturn(information);
    return new AvailablePlugin(descriptor);
  }

  private static void returnInformation(Plugin mockedPlugin, PluginInformation information) {
    when(mockedPlugin.getDescriptor().getInformation()).thenReturn(information);
  }
}
