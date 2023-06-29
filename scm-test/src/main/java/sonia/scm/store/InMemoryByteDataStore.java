/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.store;

import sonia.scm.security.KeyGenerator;
import sonia.scm.security.UUIDKeyGenerator;

import javax.xml.bind.JAXB;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InMemoryByteDataStore<T> implements DataStore<T> {

  private final Class<T> type;
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
}
