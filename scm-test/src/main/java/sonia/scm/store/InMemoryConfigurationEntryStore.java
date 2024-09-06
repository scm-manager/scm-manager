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


import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @deprecated Replaced by {@link InMemoryByteConfigurationEntryStore}
 */
@Deprecated(since = "2.44.0")
public class InMemoryConfigurationEntryStore<V> implements ConfigurationEntryStore<V> {

  private final Map<String, V> values = new HashMap<>();

  @Override
  public Collection<V> getMatchingValues(Predicate<V> predicate) {
    return values.values().stream().filter(predicate).collect(Collectors.toList());
  }

  @Override
  public String put(V item) {
    String key = UUID.randomUUID().toString();
    values.put(key, item);
    return key;
  }

  @Override
  public void put(String id, V item) {
    values.put(id, item);
  }

  @Override
  public Map<String, V> getAll() {
    return Collections.unmodifiableMap(values);
  }

  @Override
  public void clear() {
    values.clear();
  }

  @Override
  public void remove(String id) {
    values.remove(id);
  }

  @Override
  public V get(String id) {
    return values.get(id);
  }
}
