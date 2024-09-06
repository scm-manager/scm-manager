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
