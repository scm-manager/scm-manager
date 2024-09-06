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

import jakarta.inject.Inject;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.version.Version;

import static sonia.scm.version.Version.parse;

@Extension
public class PluginCenterUpdateStep implements UpdateStep {

  private final ConfigurationStoreFactory configurationStoreFactory;

  @Inject
  public PluginCenterUpdateStep(ConfigurationStoreFactory configurationStoreFactory) {
    this.configurationStoreFactory = configurationStoreFactory;
  }

  @Override
  public void doUpdate() {
    ConfigurationStore<ScmConfiguration> configurationStore = configurationStoreFactory
      .withType(ScmConfiguration.class)
      .withName("config")
      .build();
    configurationStore.getOptional()
      .ifPresent(config -> {
        config.setPluginUrl(ScmConfiguration.DEFAULT_PLUGIN_URL);
        configurationStore.set(config);
      });
  }

  @Override
  public Version getTargetVersion() {
    return parse("2.0.0");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.plugin-center";
  }
}
