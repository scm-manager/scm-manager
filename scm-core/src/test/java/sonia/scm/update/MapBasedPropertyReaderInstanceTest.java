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
    
package sonia.scm.update;

import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

class MapBasedPropertyReaderInstanceTest {

  Map<String, V1Properties> executedCalls = new HashMap<>();

  BiConsumer<String, V1Properties> consumer = (key, properties) -> executedCalls.put(key, properties);

  MapBasedPropertyReaderInstance instance = new MapBasedPropertyReaderInstance(
    ImmutableMap.of(
      "o1", new V1Properties(
        new V1Property("k1", "v1-1"),
        new V1Property("k2", "v1-2"),
        new V1Property("k3", "v1-3")
      ),
      "o2", new V1Properties(
        new V1Property("k1", "v2-1"),
        new V1Property("k2", "v2-2")
      ),
      "o3", new V1Properties(
        new V1Property("k1", "v3-1")
      )
    )
  );

  @Test
  void shouldCallBackForEachObjectIfNotFiltered() {
    instance.forEachEntry(consumer);

    Assertions.assertThat(executedCalls).hasSize(3);
  }

  @Test
  void shouldCallBackOnlyObjectsHavingAtLeastOneOfGivenKey() {
    instance.havingAnyOf("k2", "k3").forEachEntry(consumer);

    Assertions.assertThat(executedCalls).hasSize(2).containsKeys("o1", "o2");
  }

  @Test
  void shouldCallBackOnlyObjectsHavingAllOfGivenKey() {
    instance.havingAllOf("k2", "k3").forEachEntry(consumer);

    Assertions.assertThat(executedCalls).hasSize(1).containsKeys("o1");
  }

  @Test
  void shouldCombineFilters() {
    instance.havingAnyOf("k2", "k3").havingAllOf("k3").forEachEntry(consumer);

    Assertions.assertThat(executedCalls).hasSize(1).containsKeys("o1");
  }
}
