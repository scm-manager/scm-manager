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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PluginInstallationContextTest {

  @Test
  void shouldReturnInstalledPlugin() {
    Set<InstalledPlugin> installed = installed("scm-git-plugin", "1.0.0");
    Set<AvailablePlugin> pending = Collections.emptySet();

    PluginInstallationContext context = PluginInstallationContext.from(installed, pending);
    Optional<NameAndVersion> plugin = context.find("scm-git-plugin");
    assertThat(plugin).contains(new NameAndVersion("scm-git-plugin", "1.0.0"));
  }

  @Test
  void shouldReturnPendingPlugin() {
    Set<InstalledPlugin> installed = Collections.emptySet();
    Set<AvailablePlugin> pending = pending("scm-hg-plugin", "1.0.0");

    PluginInstallationContext context = PluginInstallationContext.from(installed, pending);
    Optional<NameAndVersion> plugin = context.find("scm-hg-plugin");
    assertThat(plugin).contains(new NameAndVersion("scm-hg-plugin", "1.0.0"));
  }

  @Test
  void shouldReturnPendingEvenWithInstalled() {
    Set<InstalledPlugin> installed = installed("scm-svn-plugin", "1.1.0");
    Set<AvailablePlugin> pending = pending("scm-svn-plugin", "1.2.0");

    PluginInstallationContext context = PluginInstallationContext.from(installed, pending);
    Optional<NameAndVersion> plugin = context.find("scm-svn-plugin");
    assertThat(plugin).contains(new NameAndVersion("scm-svn-plugin", "1.2.0"));
  }

  @Test
  void shouldReturnEmpty() {
    Set<InstalledPlugin> installed = Collections.emptySet();
    Set<AvailablePlugin> pending = Collections.emptySet();

    PluginInstallationContext context = PluginInstallationContext.from(installed, pending);
    Optional<NameAndVersion> plugin = context.find("scm-legacy-plugin");
    assertThat(plugin).isEmpty();
  }

  @Test
  void shouldCreateContextFromDescriptor() {
    Set<InstalledPluginDescriptor> installed = mockDescriptor(InstalledPluginDescriptor.class, "scm-svn-plugin", "1.1.0");
    Set<AvailablePluginDescriptor> pending = mockDescriptor(AvailablePluginDescriptor.class, "scm-svn-plugin", "1.2.0");

    PluginInstallationContext context = PluginInstallationContext.fromDescriptors(installed, pending);
    Optional<NameAndVersion> plugin = context.find("scm-svn-plugin");
    assertThat(plugin).contains(new NameAndVersion("scm-svn-plugin", "1.2.0"));
  }

  private Set<InstalledPlugin> installed(String name, String version) {
    return mockPlugin(InstalledPlugin.class, name, version);
  }

  private Set<AvailablePlugin> pending(String name, String version) {
    return mockPlugin(AvailablePlugin.class, name, version);
  }

  private <P extends Plugin> Set<P> mockPlugin(Class<P> pluginClass, String name, String version) {
    P plugin = mock(pluginClass, Answers.RETURNS_DEEP_STUBS);
    when(plugin.getDescriptor().getInformation().getName()).thenReturn(name);
    when(plugin.getDescriptor().getInformation().getVersion()).thenReturn(version);
    return Collections.singleton(plugin);
  }

  private <D extends PluginDescriptor> Set<D> mockDescriptor(Class<D> descriptorClass, String name, String version) {
    D desc = mock(descriptorClass, Answers.RETURNS_DEEP_STUBS);
    when(desc.getInformation().getName()).thenReturn(name);
    when(desc.getInformation().getVersion()).thenReturn(version);
    return Collections.singleton(desc);
  }

}
