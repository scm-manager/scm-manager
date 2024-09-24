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

package sonia.scm.collect;

import org.junit.jupiter.api.Test;

import java.util.Queue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EvictingQueueTest {

  @Test
  void shouldReturnFalseIfMaxSizeIsZero() {
    Queue<String> queue = EvictingQueue.create(0);
    assertThat(queue.add("a")).isFalse();
  }

  @Test
  void shouldEvictFirstAddedEntry() {
    Queue<String> queue = EvictingQueue.create(2);
    assertThat(queue.add("a")).isTrue();
    assertThat(queue.add("b")).isTrue();
    assertThat(queue.add("c")).isTrue();
    assertThat(queue).containsOnly("b", "c");
  }

  @Test
  void shouldCreateWithDefaultSize() {
    EvictingQueue<String> queue = new EvictingQueue<>();
    assertThat(queue.maxSize).isEqualTo(100);
  }

  @Test
  void shouldFailWithNegativeSize() {
    assertThrows(IllegalArgumentException.class, () -> EvictingQueue.create(-1));
  }
}
