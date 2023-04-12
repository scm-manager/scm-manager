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

package sonia.scm.update.plugin;

import org.junit.jupiter.api.Test;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.InMemoryConfigurationStoreFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static sonia.scm.config.ScmConfiguration.DEFAULT_PLUGIN_URL;

class PluginCenterJreVersionUpdateStepTest {

  InMemoryConfigurationStoreFactory configurationStoreFactory = new InMemoryConfigurationStoreFactory();
  ConfigurationStore<ScmConfiguration> configurationStore = configurationStoreFactory.withType(ScmConfiguration.class).withName("config").build();

  @Test
  void shouldKeepCustomPluginCenterUrl() {
    String customPluginUrl = "http://some.old/url";
    mockPluginUrl(customPluginUrl);

    new PluginCenterJreVersionUpdateStep(configurationStoreFactory)
      .doUpdate();

    assertThat(configurationStore.get().getPluginUrl()).isEqualTo(customPluginUrl);
  }

  @Test
  void shouldUpdatePluginCenterUrlsWithQueryParameters() {
    String oldDefaultPluginUrl = "https://plugin-center-api.scm-manager.org/api/v1/plugins/{version}?os={os}&arch={arch}";
    mockPluginUrl(oldDefaultPluginUrl);

    new PluginCenterJreVersionUpdateStep(configurationStoreFactory)
      .doUpdate();

    assertThat(configurationStore.get().getPluginUrl()).isEqualTo(DEFAULT_PLUGIN_URL);
  }

  private void mockPluginUrl(String pluginUrl) {
    ScmConfiguration scmConfiguration = new ScmConfiguration();
    scmConfiguration.setPluginUrl(pluginUrl);
    configurationStore.set(scmConfiguration);
  }

}
