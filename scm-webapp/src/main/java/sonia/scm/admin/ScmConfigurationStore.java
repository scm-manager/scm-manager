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

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

@Singleton
public final class ScmConfigurationStore {

  private static final String STORE_NAME = "config";
  private final ConfigurationStoreFactory storeFactory;
  private final ScmConfiguration scmConfiguration;

  @Inject
  public ScmConfigurationStore(ConfigurationStoreFactory configurationStoreFactory, ScmConfiguration scmConfiguration) {
    this.storeFactory = configurationStoreFactory;
    this.scmConfiguration = scmConfiguration;
  }

  public ScmConfiguration get() {
    ConfigurationStore<ScmConfiguration> store = createStore();
    return store.getOptional().orElse(new ScmConfiguration());
  }

  public void store(ScmConfiguration updatedConfig) {
    synchronized (ScmConfiguration.class) {
      // We must update the injectable "ScmConfiguration" instance, which is directly injected into several classes.
      scmConfiguration.load(updatedConfig);
      createStore().set(updatedConfig);
      updatedConfig.fireChangeEvent();
    }
  }

  private ConfigurationStore<ScmConfiguration> createStore() {
    return storeFactory.withType(ScmConfiguration.class).withName(STORE_NAME).build();
  }
}
