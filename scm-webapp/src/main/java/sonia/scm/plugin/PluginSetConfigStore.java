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

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import java.util.Optional;

@Singleton
public class PluginSetConfigStore {

  private final ConfigurationStore<PluginSetsConfig> pluginSets;

  @Inject
  PluginSetConfigStore(ConfigurationStoreFactory configurationStoreFactory) {
    pluginSets = configurationStoreFactory.withType(PluginSetsConfig.class).withName("pluginSets").build();
  }

  public Optional<PluginSetsConfig> getPluginSets() {
    return pluginSets.getOptional();
  }

  public void setPluginSets(PluginSetsConfig config) {
    this.pluginSets.set(config);
  }
}
