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

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContextProvider;
import sonia.scm.cache.CacheManager;
import sonia.scm.cache.MapCacheManager;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.util.SystemUtil;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
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
    when(loader.load(PLUGIN_URL_BASE + "2.0.0")).thenReturn(plugins);

    assertThat(pluginCenter.getAvailable()).isSameAs(plugins);
  }

  @Test
  void shouldCache() {
    Set<AvailablePlugin> first = new HashSet<>();
    when(loader.load(anyString())).thenReturn(first, new HashSet<>());

    assertThat(pluginCenter.getAvailable()).isSameAs(first);
    assertThat(pluginCenter.getAvailable()).isSameAs(first);
  }

}
