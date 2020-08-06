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
