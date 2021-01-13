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

package sonia.scm.importer;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContextProvider;
import sonia.scm.environment.Plugin;
import sonia.scm.environment.Plugins;
import sonia.scm.environment.ScmEnvironment;
import sonia.scm.plugin.InstalledPlugin;
import sonia.scm.plugin.PluginManager;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    when(pluginManager.getInstalled()).thenReturn(ImmutableList.of(first, second));
  }

  @Test
  void shouldReturnTrueIfEnvironmentIsCompatible() {
    when(scmContextProvider.getVersion()).thenReturn("2.0.0");
    ImmutableList<Plugin> plugins = ImmutableList.of(new Plugin("scm-first-plugin", "1.0.0"), new Plugin("scm-second-plugin", "1.1.0"));
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
    ImmutableList<Plugin> plugins = ImmutableList.of(new Plugin("scm-second-plugin", "1.2.0"));
    ScmEnvironment env = createScmEnvironment("2.13.0", "linux", "64", plugins);

    boolean compatible = checker.check(env);

    assertThat(compatible).isFalse();
  }

  @Test
  void shouldReturnTrueIfPluginDoNotMatch() {
    when(scmContextProvider.getVersion()).thenReturn("2.13.0");
    ImmutableList<Plugin> plugins = ImmutableList.of(new Plugin("scm-third-plugin", "42.0.0"));
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

  private ScmEnvironment createScmEnvironment(String coreVersion, String os, String arch, List<Plugin> pluginList) {
    ScmEnvironment scmEnvironment = new ScmEnvironment();
    scmEnvironment.setCoreVersion(coreVersion);
    scmEnvironment.setOs(os);
    scmEnvironment.setArch(arch);

    Plugins plugins = new Plugins();
    plugins.setPlugin(pluginList);
    scmEnvironment.setPlugins(plugins);
    return scmEnvironment;
  }

}
