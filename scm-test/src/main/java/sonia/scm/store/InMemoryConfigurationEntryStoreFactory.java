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
 * @deprecated Use {@link InMemoryByteConfigurationEntryStoreFactory} instead to verify JaxB serialization, too.
 */
@Deprecated(since = "2.44.0")
public class InMemoryConfigurationEntryStoreFactory implements ConfigurationEntryStoreFactory {

  private final Map<String, InMemoryConfigurationEntryStore> stores = new HashMap<>();

  public static InMemoryConfigurationEntryStoreFactory create() {
    return new InMemoryConfigurationEntryStoreFactory();
  }

  @Override
  public <T> ConfigurationEntryStore<T> getStore(TypedStoreParameters<T> storeParameters) {
    String name = storeParameters.getName();
    if (storeParameters.getRepositoryId() != null) {
      name = name + "-" + storeParameters.getRepositoryId();
    }
    return get(name);
  }

  public <T> InMemoryConfigurationEntryStore<T> get(String name) {
    return stores.computeIfAbsent(name, x -> new InMemoryConfigurationEntryStore<T>());
  }
}
