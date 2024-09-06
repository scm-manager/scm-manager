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
 * Stores data in memory but in contrast to {@link InMemoryConfigurationEntryStoreFactory}
 * it uses jaxb to marshal and unmarshall the objects.
 *
 * @since 2.44.0
 */
public class InMemoryByteConfigurationEntryStoreFactory implements ConfigurationEntryStoreFactory, InMemoryStoreParameterNameComputer {

  @SuppressWarnings("rawtypes")
  private final Map<String, InMemoryByteConfigurationEntryStore> stores = new HashMap<>();

  public static InMemoryByteConfigurationEntryStoreFactory create() {
    return new InMemoryByteConfigurationEntryStoreFactory();
  }

  @Override
  public <T> ConfigurationEntryStore<T> getStore(TypedStoreParameters<T> storeParameters) {
    String name = computeKey(storeParameters);
    Class<T> type = storeParameters.getType();
    return get(type, name);
  }

  @SuppressWarnings("unchecked")
  public <T> ConfigurationEntryStore<T> get(Class<T> type, String name) {
    InMemoryByteConfigurationEntryStore<T> store = stores.computeIfAbsent(name, n -> new InMemoryByteConfigurationEntryStore<>(type));
    store.overrideType(type);
    return store;
  }
}
