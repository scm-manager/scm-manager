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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContextProvider;
import sonia.scm.cache.CacheManager;
import sonia.scm.cache.MapCacheManager;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.config.ScmConfigurationChangedEvent;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PluginCenterTest {

  private static final String PLUGIN_URL_BASE = "https://plugins.hitchhiker.com/";
  private static final String PLUGIN_URL = PLUGIN_URL_BASE + "{version}";

  @Mock
  private PluginCenterLoader loader;

  @Mock
  private SCMContextProvider contextProvider;

  private ScmConfiguration configuration;

  private CacheManager cacheManager;

  private PluginCenter pluginCenter;

  @BeforeEach
  void setUpPluginCenter() {
    when(contextProvider.getVersion()).thenReturn("2.0.0");

    cacheManager = new MapCacheManager();

    configuration = new ScmConfiguration();
    configuration.setPluginUrl(PLUGIN_URL);

    pluginCenter = new PluginCenter(contextProvider, cacheManager, configuration, loader);
  }

  @Test
  void shouldFetchPlugins() {
    Set<AvailablePlugin> plugins = new HashSet<>();
    Set<PluginSet> pluginSets = new HashSet<>();

    PluginCenterResult pluginCenterResult = new PluginCenterResult(plugins, pluginSets);
    when(loader.load(PLUGIN_URL_BASE + "2.0.0")).thenReturn(pluginCenterResult);

    assertThat(pluginCenter.getAvailablePlugins()).isSameAs(plugins);
    assertThat(pluginCenter.getAvailablePluginSets()).isSameAs(pluginSets);
  }

  @Test
  @SuppressWarnings("unchecked")
  void shouldCache() {
    Set<AvailablePlugin> plugins = new HashSet<>();
    Set<PluginSet> pluginSets = new HashSet<>();

    PluginCenterResult first = new PluginCenterResult(plugins, pluginSets);
    when(loader.load(anyString())).thenReturn(first, new PluginCenterResult());

    assertThat(pluginCenter.getAvailablePlugins()).isSameAs(plugins);
    assertThat(pluginCenter.getAvailablePlugins()).isSameAs(plugins);
    assertThat(pluginCenter.getAvailablePluginSets()).isSameAs(pluginSets);
  }

  @Test
  @SuppressWarnings("unchecked")
  void shouldClearCacheOnConfigChange() {
    Set<AvailablePlugin> plugins = new HashSet<>();
    Set<PluginSet> pluginSets = new HashSet<>();

    PluginCenterResult first = new PluginCenterResult(plugins, pluginSets);
    when(loader.load(anyString())).thenReturn(first, new PluginCenterResult());

    assertThat(pluginCenter.getAvailablePlugins()).isSameAs(plugins);
    assertThat(pluginCenter.getAvailablePluginSets()).isSameAs(pluginSets);
    pluginCenter.handle(new ScmConfigurationChangedEvent(null));
    assertThat(pluginCenter.getAvailablePlugins()).isNotSameAs(plugins);
    assertThat(pluginCenter.getAvailablePluginSets()).isNotSameAs(pluginSets);
  }

  @Test
  void shouldLoadOnRefresh() {
    Set<AvailablePlugin> plugins = new HashSet<>();
    Set<PluginSet> pluginSets = new HashSet<>();

    PluginCenterResult pluginCenterResult = new PluginCenterResult(plugins, pluginSets);
    when(loader.load(PLUGIN_URL_BASE + "2.0.0")).thenReturn(pluginCenterResult);

    pluginCenter.refresh();

    verify(loader).load(PLUGIN_URL_BASE + "2.0.0");
  }

}
