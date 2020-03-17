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
package sonia.scm.repository;

import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class RepositoryPermissionTest {

  @Test
  void shouldBeEqualWithSameVerbs() {
    RepositoryPermission permission1 = new RepositoryPermission("name", asList("one", "two"), false);
    RepositoryPermission permission2 = new RepositoryPermission("name", asList("two", "one"), false);

    assertThat(permission1).isEqualTo(permission2);
  }

  @Test
  void shouldHaveSameHashCodeWithSameVerbs() {
    long hash1 = new RepositoryPermission("name", asList("one", "two"), false).hashCode();
    long hash2 = new RepositoryPermission("name", asList("two", "one"), false).hashCode();

    assertThat(hash1).isEqualTo(hash2);
  }

  @Test
  void shouldNotBeEqualWithSameVerbs() {
    RepositoryPermission permission1 = new RepositoryPermission("name", asList("one", "two"), false);
    RepositoryPermission permission2 = new RepositoryPermission("name", asList("three", "one"), false);

    assertThat(permission1).isNotEqualTo(permission2);
  }

  @Test
  void shouldNotBeEqualWithDifferentType() {
    RepositoryPermission permission1 = new RepositoryPermission("name", asList("one"), false);
    RepositoryPermission permission2 = new RepositoryPermission("name", asList("one"), true);

    assertThat(permission1).isNotEqualTo(permission2);
  }

  @Test
  void shouldNotBeEqualWithDifferentName() {
    RepositoryPermission permission1 = new RepositoryPermission("name1", asList("one"), false);
    RepositoryPermission permission2 = new RepositoryPermission("name2", asList("one"), false);

    assertThat(permission1).isNotEqualTo(permission2);
  }

  @Test
  void shouldBeEqualWithRedundantVerbs() {
    RepositoryPermission permission1 = new RepositoryPermission("name1", asList("one", "two"), false);
    RepositoryPermission permission2 = new RepositoryPermission("name1", asList("one", "two", "two"), false);

    assertThat(permission1).isEqualTo(permission2);
  }
}
