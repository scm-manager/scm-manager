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
