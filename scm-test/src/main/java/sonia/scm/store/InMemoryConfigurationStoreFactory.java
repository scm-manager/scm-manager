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
 * In memory configuration store factory for testing purposes.
 *
 *
 * @deprecated Use the {@link InMemoryByteConfigurationStoreFactory} to verify JaxB serialization, too.
 */
@Deprecated(since = "2.44.0")
public class InMemoryConfigurationStoreFactory implements ConfigurationStoreFactory {

  private final Map<String, InMemoryConfigurationStore> stores = new HashMap<>();

  public static InMemoryConfigurationStoreFactory create() {
    return new InMemoryConfigurationStoreFactory();
  }

  @Override
  public ConfigurationStore getStore(TypedStoreParameters storeParameters) {
    String name = storeParameters.getName();
    String id = storeParameters.getRepositoryId();
    return get(name, id);
  }

  public ConfigurationStore get(String name, String id) {
    return stores.computeIfAbsent(buildKey(name, id), x -> new InMemoryConfigurationStore());
  }

  private String buildKey(String name, String id) {
    return id == null? name: name + "-" + id;
  }
}
