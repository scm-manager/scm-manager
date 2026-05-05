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

package sonia.scm.lifecycle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.lifecycle.Scm4xCompatibilityStartupAction.Result;
import sonia.scm.plugin.AvailablePlugin;
import sonia.scm.plugin.InstalledPlugin;
import sonia.scm.plugin.Plugin;
import sonia.scm.plugin.PluginCondition;
import sonia.scm.plugin.PluginInformation;
import sonia.scm.plugin.PluginManager;
import sonia.scm.plugin.PluginTestHelper;
import sonia.scm.web.security.AdministrationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sonia.scm.lifecycle.Scm4xCompatibilityStartupAction.Status.NO_ACTION_NEEDED;
import static sonia.scm.lifecycle.Scm4xCompatibilityStartupAction.Status.PLUGIN_CENTER_DEACTIVATED;
import static sonia.scm.lifecycle.Scm4xCompatibilityStartupAction.Status.UPDATED;
import static sonia.scm.lifecycle.Scm4xCompatibilityStartupAction.Status.UPDATE_NOT_POSSIBLE;

@ExtendWith(MockitoExtension.class)
class Scm4xCompatibilityStartupActionTest {

  @Mock
  private PluginManager pluginManager;
  @Mock
  private AdministrationContext adminContext;
  @Mock
  private Restarter restarter;
  @Mock
  private RandomPasswordGenerator randomPasswordGenerator;
  private ScmConfiguration configuration;

  private Scm4xCompatibilityStartupAction compatabilityAction;

  private InstalledPlugin mockInstalledPlugin(String name, String minVersion, boolean is4xCompatible) {
    InstalledPlugin plugin = PluginTestHelper.createInstalled(name);
    mockMinVersion(plugin, minVersion);
    mockPluginInformation(plugin, name, is4xCompatible);

    return plugin;
  }

  private AvailablePlugin mockAvailablePlugin(String name, String minVersion, boolean is4xCompatible) {
    AvailablePlugin plugin = PluginTestHelper.createAvailable(name);
    mockMinVersion(plugin, minVersion);
    mockPluginInformation(plugin, name, is4xCompatible);

    return plugin;
  }

  private void mockMinVersion(Plugin plugin, String minVersion) {
    PluginCondition condition = mock(PluginCondition.class);
    lenient().when(plugin.getDescriptor().getCondition()).thenReturn(condition);
    lenient().when(condition.getMinVersion()).thenReturn(minVersion);
  }

  private void mockPluginInformation(Plugin plugin, String name, boolean isCompatible) {
    PluginInformation pluginInformation = mock(PluginInformation.class);
    lenient().when(plugin.getDescriptor().getInformation()).thenReturn(pluginInformation);
    lenient().when(pluginInformation.isScm4Compatible()).thenReturn(isCompatible);
    lenient().when(pluginInformation.getName()).thenReturn(name);
  }

  @BeforeEach
  void setup() {
    configuration = new ScmConfiguration();
    configuration.setPluginUrl("https://plugin-center.com/");
    compatabilityAction = new Scm4xCompatibilityStartupAction(
      pluginManager, configuration, adminContext, restarter, randomPasswordGenerator
    );

    doAnswer(invocation -> {
      invocation.getArgument(0, PrivilegedStartupAction.class).run();
      return null;
    }).when(adminContext).runAsAdmin(any(PrivilegedStartupAction.class));
  }

  @Test
  void shouldReturnNoActionBecauseNoPluginsAreInstalled() {
    when(pluginManager.getInstalled()).thenReturn(List.of());

    assertThat(compatabilityAction.done()).isTrue();
    assertThat(compatabilityAction.getCompatibilityResult()).isEqualTo(
      new Result(NO_ACTION_NEEDED, List.of())
    );
    verify(pluginManager, never()).updateAll();
  }

  @Test
  void shouldGenerateRandomTokenOnce() {
    when(pluginManager.getInstalled()).thenReturn(List.of());
    when(randomPasswordGenerator.createRandomPassword()).thenReturn("expected");

    compatabilityAction.done();
    assertThat(compatabilityAction.getToken()).isEqualTo("expected");
    compatabilityAction.done();
    assertThat(compatabilityAction.getToken()).isEqualTo("expected");
  }

  @Test
  void shouldReturnNoActionBecauseAllPluginsAre4x() {
    InstalledPlugin scm4xPlugin = mockInstalledPlugin("scm-4x-plugin", "4.0.0", false);
    when(pluginManager.getInstalled()).thenReturn(List.of(scm4xPlugin));

    assertThat(compatabilityAction.done()).isTrue();
    assertThat(compatabilityAction.getCompatibilityResult()).isEqualTo(
      new Result(NO_ACTION_NEEDED, List.of())
    );
    verify(pluginManager, never()).updateAll();
  }

  @Test
  void shouldReturnNoActionBecauseAllPluginsAreMarkedAsCompatible() {
    InstalledPlugin compatibleScm3xPlugin = mockInstalledPlugin("scm-compatible-3x-plugin", "3.0.0", true);
    when(pluginManager.getInstalled()).thenReturn(List.of(compatibleScm3xPlugin));

    assertThat(compatabilityAction.done()).isTrue();
    assertThat(compatabilityAction.getCompatibilityResult()).isEqualTo(
      new Result(NO_ACTION_NEEDED, List.of())
    );
    verify(pluginManager, never()).updateAll();
  }

  @Test
  void shouldReturnUpdatedAndRestartBecauseAllIncompatiblePluginsWereUpdated() {
    InstalledPlugin pluginWith4xMigration = mockInstalledPlugin("scm-with-4x-migration-plugin", "3.0.0", false);
    AvailablePlugin migrated4xPlugin = mockAvailablePlugin("scm-with-4x-migration-plugin", "4.0.0", false);

    InstalledPlugin pluginWithUnsetFlag = mockInstalledPlugin("scm-with-unset-flag-plugin", "3.0.0", false);
    AvailablePlugin pluginWithSetFlag = mockAvailablePlugin("scm-with-unset-flag-plugin", "3.0.0", true);

    when(pluginManager.getInstalled()).thenReturn(List.of(pluginWith4xMigration, pluginWithUnsetFlag));
    when(pluginManager.getAvailable()).thenReturn(List.of(migrated4xPlugin, pluginWithSetFlag));
    when(restarter.isSupported()).thenReturn(true);

    assertThat(compatabilityAction.done()).isFalse();
    assertThat(compatabilityAction.getCompatibilityResult()).isEqualTo(
      new Result(UPDATED, List.of())
    );
    verify(pluginManager).updateAll();
    verify(restarter).restart(Scm4xCompatibilityStartupAction.class, "Finish SCM 4 plugin update");
  }

  @Test
  void shouldReturnUpdatedButWithoutRestartBecauseRestartIsNotSupported() {
    InstalledPlugin pluginWith4xMigration = mockInstalledPlugin("scm-with-4x-migration-plugin", "3.0.0", false);
    AvailablePlugin migrated4xPlugin = mockAvailablePlugin("scm-with-4x-migration-plugin", "4.0.0", false);

    when(pluginManager.getInstalled()).thenReturn(List.of(pluginWith4xMigration));
    when(pluginManager.getAvailable()).thenReturn(List.of(migrated4xPlugin));
    when(restarter.isSupported()).thenReturn(false);

    assertThat(compatabilityAction.done()).isFalse();
    assertThat(compatabilityAction.getCompatibilityResult()).isEqualTo(
      new Result(UPDATED, List.of())
    );
    verify(pluginManager).updateAll();
    verify(restarter, never()).restart(any(), any());
  }

  @Test
  void shouldReturnUpdateNotPossibleBecauseOfIncompatiblePluginWithoutCompatibleVersion() {
    InstalledPlugin notUpdatable3xPlugin = mockInstalledPlugin("scm-not-updateable-3x-plugin", "3.0.0", false);
    AvailablePlugin differentPlugin = mockAvailablePlugin("scm-different-plugin", "4.0.0", true);

    when(pluginManager.getInstalled()).thenReturn(List.of(notUpdatable3xPlugin));
    when(pluginManager.getAvailable()).thenReturn(List.of(differentPlugin));

    assertThat(compatabilityAction.done()).isFalse();
    assertThat(compatabilityAction.getCompatibilityResult()).isEqualTo(
      new Result(UPDATE_NOT_POSSIBLE, List.of(notUpdatable3xPlugin))
    );
    verify(pluginManager, never()).updateAll();
  }

  @Test
  void shouldReturnPluginCenterDeactivatedBecauseUrlIsNull() {
    shouldReturnPluginCenterDeactivatedBecauseUrlIsInvalid(null);
  }

  @ParameterizedTest
  @ValueSource( strings = {"", "test.de"})
  void shouldReturnPluginCenterDeactivatedBecauseUrlIsInvalid(String url) {
    InstalledPlugin incompatiblePlugin = mockInstalledPlugin("incompatible-plugin", "3.0.0", false);
    when(pluginManager.getInstalled()).thenReturn(List.of(incompatiblePlugin));
    configuration.setPluginUrl(url);

    assertThat(compatabilityAction.done()).isFalse();
    assertThat(compatabilityAction.getCompatibilityResult()).isEqualTo(
      new Result(PLUGIN_CENTER_DEACTIVATED, List.of(incompatiblePlugin))
    );

    verify(pluginManager, never()).updateAll();
  }
}
