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

package sonia.scm.importexport;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContextProvider;
import sonia.scm.plugin.InstalledPlugin;
import sonia.scm.plugin.PluginManager;
import sonia.scm.version.Version;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScmEnvironmentCompatibilityCheckerTest {

  @Mock
  private PluginManager pluginManager;
  @Mock
  private SCMContextProvider scmContextProvider;

  @InjectMocks
  private ScmEnvironmentCompatibilityChecker checker;

  @BeforeEach
  void preparePluginManager() {
    InstalledPlugin first = mockPlugin("scm-first-plugin", "1.0.0");
    InstalledPlugin second = mockPlugin("scm-second-plugin", "1.1.0");
    lenient().when(pluginManager.getInstalled()).thenReturn(ImmutableList.of(first, second));
  }

  @Test
  void shouldReturnTrueIfEnvironmentIsSame() {
    when(scmContextProvider.getVersion()).thenReturn("2.0.0");
    ImmutableList<EnvironmentPluginDescriptor> plugins = ImmutableList.of(
      new EnvironmentPluginDescriptor("scm-first-plugin", "1.0.0"),
      new EnvironmentPluginDescriptor("scm-second-plugin", "1.1.0")
    );
    ScmEnvironment env = createScmEnvironment("2.0.0", "linux", "64", plugins);

    boolean compatible = checker.check(env);

    assertThat(compatible).isTrue();
  }

  @Test
  void shouldReturnTrueIfEnvironmentIsNewer() {
    when(scmContextProvider.getVersion()).thenReturn("2.1.0");
    ImmutableList<EnvironmentPluginDescriptor> plugins = ImmutableList.of(
      new EnvironmentPluginDescriptor("scm-first-plugin", "0.9.0"),
      new EnvironmentPluginDescriptor("scm-second-plugin", "1.0.1")
    );
    ScmEnvironment env = createScmEnvironment("2.0.0", "linux", "64", plugins);

    boolean compatible = checker.check(env);

    assertThat(compatible).isTrue();
  }

  @Test
  void shouldReturnFalseIfCoreVersionIncompatible() {
    when(scmContextProvider.getVersion()).thenReturn("2.0.0");
    ScmEnvironment env = createScmEnvironment("2.13.0", "linux", "64", Collections.emptyList());

    boolean compatible = checker.check(env);

    assertThat(compatible).isFalse();
  }

  @Test
  void shouldReturnFalseIfPluginIsIncompatible() {
    when(scmContextProvider.getVersion()).thenReturn("2.13.0");
    ImmutableList<EnvironmentPluginDescriptor> plugins = ImmutableList.of(new EnvironmentPluginDescriptor("scm-second-plugin", "1.2.0"));
    ScmEnvironment env = createScmEnvironment("2.13.0", "linux", "64", plugins);

    boolean compatible = checker.check(env);

    assertThat(compatible).isFalse();
  }

  @Test
  void shouldReturnTrueIfPluginDoNotMatch() {
    when(scmContextProvider.getVersion()).thenReturn("2.13.0");
    ImmutableList<EnvironmentPluginDescriptor> plugins = ImmutableList.of(new EnvironmentPluginDescriptor("scm-third-plugin", "42.0.0"));
    ScmEnvironment env = createScmEnvironment("2.13.0", "linux", "64", plugins);

    boolean compatible = checker.check(env);

    assertThat(compatible).isTrue();
  }

  private InstalledPlugin mockPlugin(String name, String version) {
    InstalledPlugin plugin = mock(InstalledPlugin.class, Answers.RETURNS_DEEP_STUBS);
    lenient().when(plugin.getDescriptor().getInformation().getName()).thenReturn(name);
    lenient().when(plugin.getDescriptor().getInformation().getVersion()).thenReturn(version);
    return plugin;
  }

  private ScmEnvironment createScmEnvironment(String coreVersion, String os, String arch, List<EnvironmentPluginDescriptor> pluginList) {
    ScmEnvironment scmEnvironment = new ScmEnvironment();
    scmEnvironment.setCoreVersion(coreVersion);
    scmEnvironment.setOs(os);
    scmEnvironment.setArch(arch);

    EnvironmentPluginsDescriptor environmentPluginsDescriptor = new EnvironmentPluginsDescriptor();
    environmentPluginsDescriptor.setPlugin(pluginList);
    scmEnvironment.setPlugins(environmentPluginsDescriptor);
    return scmEnvironment;
  }
}
