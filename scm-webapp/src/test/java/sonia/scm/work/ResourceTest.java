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
