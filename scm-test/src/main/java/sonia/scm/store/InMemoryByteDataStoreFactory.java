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
 * Stores data in memory but in contrast to {@link InMemoryDataStoreFactory}
 * it uses jaxb to marshal and unmarshall the objects.
 *
 * @since 2.18.0
 */
public class InMemoryByteDataStoreFactory implements DataStoreFactory, InMemoryStoreParameterNameComputer {

  @SuppressWarnings("rawtypes")
  private final Map<String, InMemoryByteDataStore> stores = new HashMap<>();

  @Override
  public <T> DataStore<T> getStore(TypedStoreParameters<T> storeParameters) {
    String name = computeKey(storeParameters);
    Class<T> type = storeParameters.getType();
    return getStore(type, name);
  }

  @SuppressWarnings("unchecked")
  public  <T> DataStore<T> getStore(Class<T> type, String name) {
    InMemoryByteDataStore<T> store = stores.computeIfAbsent(name, n -> new InMemoryByteDataStore<>(type));
    store.overrideType(type);
    return store;
  }
}
