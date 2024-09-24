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

package sonia.scm.lifecycle.jwt;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

@Singleton
public class JwtSettingsStore {

  static final String STORE_NAME = "jwt-settings";
  private final ConfigurationStore<JwtSettings> store;

  @Inject
  public JwtSettingsStore(ConfigurationStoreFactory storeFactory) {
    store = storeFactory.withType(JwtSettings.class).withName(STORE_NAME).build();
  }

  public JwtSettings get() {
    return store.getOptional().orElse(new JwtSettings());
  }

  public void set(JwtSettings settings) {
    store.set(settings);
  }
}
