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

package sonia.scm.work;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceTest {

  @Test
  void shouldReturnResourceName() {
    assertThat(res("a")).hasToString("a");
  }

  @Test
  void shouldReturnResourceNameAndId() {
    assertThat(res("a", "b")).hasToString("a:b");
  }

  @Nested
  class IsBlockedByTests {

    @Test
    void shouldReturnTrue() {
      assertThat(res("a").isBlockedBy(res("a"))).isTrue();
      assertThat(res("a", "b").isBlockedBy(res("a", "b"))).isTrue();
      assertThat(res("a").isBlockedBy(res("a", "b"))).isTrue();
      assertThat(res("a", "b").isBlockedBy(res("a"))).isTrue();
    }

    @Test
    void shouldReturnFalse() {
      assertThat(res("a").isBlockedBy(res("b"))).isFalse();
      assertThat(res("a", "b").isBlockedBy(res("a", "c"))).isFalse();
      assertThat(res("a", "b").isBlockedBy(res("c", "b"))).isFalse();
    }

  }

  private Resource res(String name) {
    return new Resource(name);
  }

  private Resource res(String name, String id) {
    return new Resource(name, id);
  }

}
