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

class PluginCenterUpdateStepTest {

  @Test
  void shouldNotCreateConfigIfNoConfigIsPresent() {
    InMemoryConfigurationStoreFactory configurationStoreFactory = new InMemoryConfigurationStoreFactory();
    new PluginCenterUpdateStep(configurationStoreFactory)
      .doUpdate();
    ScmConfiguration config = configurationStoreFactory.withType(ScmConfiguration.class).withName("config").build().get();
    assertThat(config).isNull();
  }

  @Test
  void shouldOverwriteOldPluginCenterUrl() {
    InMemoryConfigurationStoreFactory configurationStoreFactory = new InMemoryConfigurationStoreFactory();
    ConfigurationStore<ScmConfiguration> configurationStore = configurationStoreFactory.withType(ScmConfiguration.class).withName("config").build();
    ScmConfiguration scmConfiguration = new ScmConfiguration();
    scmConfiguration.setPluginUrl("http://some.old/url");
    configurationStore.set(scmConfiguration);
    new PluginCenterUpdateStep(configurationStoreFactory)
      .doUpdate();
    ScmConfiguration config = configurationStore.get();
    assertThat(config.getPluginUrl()).isEqualTo(ScmConfiguration.DEFAULT_PLUGIN_URL);
  }
}
