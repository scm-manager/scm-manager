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

import jakarta.xml.bind.JAXB;
import sonia.scm.security.KeyGenerator;
import sonia.scm.security.UUIDKeyGenerator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InMemoryByteDataStore<T> implements DataStore<T> {

  private Class<T> type;
  private final KeyGenerator generator = new UUIDKeyGenerator();
  private final Map<String, byte[]> store = new HashMap<>();

  InMemoryByteDataStore(Class<T> type) {
    this.type = type;
  }

  @Override
  public String put(T item) {
    String id = generator.createKey();
    put(id, item);
    return id;
  }

  @Override
  public void put(String id, T item) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    JAXB.marshal(item, baos);
    store.put(id, baos.toByteArray());
  }

  /**
   * This method can be used to mock stores with old types to test update steps or otherwise the compatability of
   * objects with old versions.
   */
  public void putOldObject(String id, Object item) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    JAXB.marshal(item, baos);
    store.put(id, baos.toByteArray());
  }

  /**
   * This method can be used to mock stores with raw xml data to test update steps or otherwise the compatability of
   * objects with old versions.
   */
  public void putRawXml(String id, String xml) {
    store.put(id, xml.getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public Map<String, T> getAll() {
    Map<String, T> all = new HashMap<>();
    for (String id : store.keySet()) {
      all.put(id, get(id));
    }
    return Collections.unmodifiableMap(all);
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
    byte[] bytes = store.get(id);
    if (bytes != null) {
      return JAXB.unmarshal(new ByteArrayInputStream(bytes), type);
    }
    return null;
  }

  void overrideType(Class<T> type) {
    this.type = type;
  }
}
