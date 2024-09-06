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

import sonia.scm.security.KeyGenerator;
import sonia.scm.security.UUIDKeyGenerator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * In memory store implementation of {@link DataStore}.
 *
 *
 * @param <T> type of stored object
 * @deprecated use {@link InMemoryByteDataStore} instead.
 */
@Deprecated
public class InMemoryDataStore<T> implements DataStore<T> {

  private final Map<String, T> store = new HashMap<>();
  private KeyGenerator generator = new UUIDKeyGenerator();

  @Override
  public String put(T item) {
    String key = generator.createKey();
    store.put(key, item);
    return key;
  }

  @Override
  public void put(String id, T item) {
    store.put(id, item);
  }

  @Override
  public Map<String, T> getAll() {
    return Collections.unmodifiableMap(store);
  }

  @Override
  public void clear() {
    store.clear();
  }

  @Override
  public void remove(String id) {
    store.remove(id);
  }

  @Override
  public T get(String id) {
    return store.get(id);
  }
}
