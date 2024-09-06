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

package sonia.scm.store;

import java.util.HashMap;
import java.util.Map;

/**
 * In memory configuration store factory for testing purposes that uses JaxB for serialization.
 *
 * @since 2.44.0
 */
public class InMemoryByteConfigurationStoreFactory implements ConfigurationStoreFactory, InMemoryStoreParameterNameComputer {

  @SuppressWarnings("rawtypes")
  private final Map<String, InMemoryByteConfigurationStore> stores = new HashMap<>();

  @Override
  public <T> ConfigurationStore<T> getStore(TypedStoreParameters<T> storeParameters) {
    String name = computeKey(storeParameters);
    Class<T> type = storeParameters.getType();
    return getStore(type, name);
  }

  @SuppressWarnings("unchecked")
  public <T> ConfigurationStore<T> getStore(Class<T> type, String name) {
    InMemoryByteConfigurationStore<T> store = stores.computeIfAbsent(name, n -> new InMemoryByteConfigurationStore<>(type));
    store.overrideType(type);
    return store;
  }
}
