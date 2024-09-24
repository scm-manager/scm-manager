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

package sonia.scm.admin;

import org.junit.jupiter.api.Test;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.store.InMemoryConfigurationStoreFactory;

import static org.assertj.core.api.Assertions.assertThat;

class ScmConfigurationStoreTest {

  @Test
  void shouldGetDefaultConfig() {
    ScmConfiguration scmConfiguration = new ScmConfigurationStore(new InMemoryConfigurationStoreFactory(), new ScmConfiguration()).get();

    assertThat(scmConfiguration.getPluginUrl()).isEqualTo("https://plugin-center-api.scm-manager.org/api/v1/plugins/{version}?os={os}&arch={arch}&jre={jre}");
  }

  @Test
  void shouldUpdateConfig() {
    ScmConfigurationStore store = new ScmConfigurationStore(new InMemoryConfigurationStoreFactory(), new ScmConfiguration());

    ScmConfiguration config = new ScmConfiguration();
    config.setBaseUrl("test.de");
    config.setProxyPassword("secret");
    store.store(config);

    ScmConfiguration scmConfiguration = store.get();

    assertThat(scmConfiguration.getBaseUrl()).isEqualTo("test.de");
    assertThat(scmConfiguration.getProxyPassword()).isEqualTo("secret");
  }

}
