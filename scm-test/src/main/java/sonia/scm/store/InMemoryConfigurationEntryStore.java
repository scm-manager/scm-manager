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


import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
