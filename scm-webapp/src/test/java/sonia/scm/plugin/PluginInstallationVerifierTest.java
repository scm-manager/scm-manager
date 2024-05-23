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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static sonia.scm.plugin.PluginInstallationContext.empty;

@ExtendWith(MockitoExtension.class)
class PluginInstallationVerifierTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private InstalledPluginDescriptor descriptor;

  // hog stands for "Heart of Gold"
  private static final String HOG_PLUGIN = "scm-hog-plugin";

  // iid stands for "Infinite Improbability Drive"
  private static final String IID_PLUGIN = "scm-iid-plugin";

  @BeforeEach
  void setUpDescriptor() {
    PluginInformation information = new PluginInformation();
    information.setName(HOG_PLUGIN);
    information.setVersion("3.0.0");
    when(descriptor.getInformation()).thenReturn(information);
  }

  @Test
  void shouldFailOnCondition() {
    PluginInstallationContext context = empty();
    assertThrows(PluginConditionFailedException.class, () -> PluginInstallationVerifier.verify(context, descriptor));
  }

  @Test
  void shouldFailOnMissingDependency() {
    matchConditions();
    when(descriptor.getDependenciesWithVersion()).thenReturn(Collections.singleton(new NameAndVersion(IID_PLUGIN)));
    PluginInstallationContext context = empty();

    DependencyNotFoundException exception = assertThrows(
      DependencyNotFoundException.class, () -> PluginInstallationVerifier.verify(context, descriptor)
    );
    assertThat(exception.getPlugin()).isEqualTo(HOG_PLUGIN);
    assertThat(exception.getMissingDependency()).isEqualTo(IID_PLUGIN);
  }

  private void matchConditions() {
    when(descriptor.getCondition()).thenReturn(new PluginCondition());
  }

  @Test
  void shouldFailOnDependencyVersionMismatch() {
    matchConditions();

    // mock installation of iid 1.0.0
    PluginInstallationContext context = mockInstallationOf(IID_PLUGIN, "1.0.0");

    // mock dependency of iid 1.1.0
    mockDependingOf(IID_PLUGIN, "1.1.0");

    DependencyVersionMismatchException exception = assertThrows(
      DependencyVersionMismatchException.class, () -> PluginInstallationVerifier.verify(context, descriptor)
    );
    assertThat(exception.getPlugin()).isEqualTo(HOG_PLUGIN);
    assertThat(exception.getDependency()).isEqualTo(IID_PLUGIN);
    assertThat(exception.getMinVersion()).isEqualTo("1.1.0");
    assertThat(exception.getCurrentVersion()).isEqualTo("1.0.0");
  }

  @Test
  void shouldFailOnOptionalDependencyVersionMismatch() {
    matchConditions();

    // mock installation of iid 1.0.0
    PluginInstallationContext context = mockInstallationOf(IID_PLUGIN, "1.0.0");

    // mock dependency of iid 1.1.0
    mockOptionalDependingOf(IID_PLUGIN, "1.1.0");

    DependencyVersionMismatchException exception = assertThrows(
      DependencyVersionMismatchException.class, () -> PluginInstallationVerifier.verify(context, descriptor)
    );
    assertThat(exception.getPlugin()).isEqualTo(HOG_PLUGIN);
    assertThat(exception.getDependency()).isEqualTo(IID_PLUGIN);
    assertThat(exception.getMinVersion()).isEqualTo("1.1.0");
    assertThat(exception.getCurrentVersion()).isEqualTo("1.0.0");
  }

  @Test
  @SuppressWarnings("squid:S2699") // we are happy if no exception is thrown
  void shouldVerifyPlugin() {
    matchConditions();

    PluginInstallationContext context = empty();
    PluginInstallationVerifier.verify(context, descriptor);
  }

  @Test
  @SuppressWarnings("squid:S2699") // we are happy if no exception is thrown
  void shouldVerifyPluginWithDependencies() {
    matchConditions();

    // mock installation of iid 1.1.0
    PluginInstallationContext context = mockInstallationOf(IID_PLUGIN, "1.1.0");

    // mock dependency of iid 1.1.0
    mockDependingOf(IID_PLUGIN, "1.1.0");

    PluginInstallationVerifier.verify(context, descriptor);
  }

  @Test
  @SuppressWarnings("squid:S2699") // we are happy if no exception is thrown
  void shouldVerifyPluginWithSnapshotDependencies() {
    matchConditions();

    PluginInstallationContext context = mockInstallationOf(IID_PLUGIN, "1.0.0-SNAPSHOT");
    mockDependingOf(IID_PLUGIN, "1.0.0-20201022.094711-15");

    PluginInstallationVerifier.verify(context, descriptor);
  }

  @Test
  @SuppressWarnings("squid:S2699") // we are happy if no exception is thrown
  void shouldVerifyPluginWithOptionalDependency() {
    matchConditions();

    PluginInstallationContext context = PluginInstallationContext.empty();

    // mock dependency of iid 1.1.0
    mockOptionalDependingOf(IID_PLUGIN, "1.1.0");

    PluginInstallationVerifier.verify(context, descriptor);
  }

  private void mockOptionalDependingOf(String plugin, String version) {
    when(descriptor.getOptionalDependenciesWithVersion()).thenReturn(Collections.singleton(new NameAndVersion(plugin, version)));
  }

  private void mockDependingOf(String plugin, String version) {
    when(descriptor.getDependenciesWithVersion()).thenReturn(Collections.singleton(new NameAndVersion(plugin, version)));
  }

  private PluginInstallationContext mockInstallationOf(String plugin, String version) {
    PluginInstallationContext context = mock(PluginInstallationContext.class);
    when(context.find(IID_PLUGIN)).thenReturn(Optional.of(new NameAndVersion(plugin, version)));
    return context;
  }

}
