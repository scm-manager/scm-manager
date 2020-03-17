/**
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
package sonia.scm.update;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class MapBasedPropertyReaderInstance implements V1PropertyReader.Instance {
  private final Stream<Map.Entry<String, V1Properties>> all;

  public MapBasedPropertyReaderInstance(Map<String, V1Properties> all) {
    this(all.entrySet().stream());
  }

  private MapBasedPropertyReaderInstance(Stream<Map.Entry<String, V1Properties>> all) {
    this.all = all;
  }

  @Override
  public void forEachEntry(BiConsumer<String, V1Properties> propertiesForNameConsumer) {
    all.forEach(e -> call(e.getKey(), e.getValue(), propertiesForNameConsumer));
  }

  @Override
  public V1PropertyReader.Instance havingAnyOf(String... keys) {
    return new MapBasedPropertyReaderInstance(all.filter(e -> e.getValue().hasAny(keys)));
  }

  @Override
  public V1PropertyReader.Instance havingAllOf(String... keys) {
    return new MapBasedPropertyReaderInstance(all.filter(e -> e.getValue().hasAll(keys)));
  }

  private void call(String repositoryId, V1Properties properties, BiConsumer<String, V1Properties> propertiesForNameConsumer) {
    propertiesForNameConsumer.accept(repositoryId, properties);
  }
}
