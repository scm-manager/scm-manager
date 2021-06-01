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
  void shouldFailWithSizeZero() {
    assertThrows(IllegalArgumentException.class, () -> EvictingQueue.create(0));
  }

  @Test
  void shouldFailWithNegativeSize() {
    assertThrows(IllegalArgumentException.class, () -> EvictingQueue.create(-1));
  }
}
