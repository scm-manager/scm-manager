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

package sonia.scm.search;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static sonia.scm.search.Ids.asString;

class IdsTest {

  @Test
  void shouldUseNameFromAnnotation() {
    Id<String> id = Id.of(String.class, "0")
      .and(Z.class, "zzz");
    assertThat(Ids.others(id)).containsEntry("c", "zzz");
  }

  @Test
  void shouldEscapeSemicolon() {
    Id<String> id = Id.of(String.class, "a;b;c");
    assertThat(Ids.asString(id)).isEqualTo("a\\;b\\;c");
  }

  @Test
  void shouldSortInAlphabeticalOrder() {
    Id<String> id = Id.of(String.class, "0")
      .and(D.class, "4")
      .and(A.class, "1")
      .and(B.class, "2")
      .and(Z.class, "3");

    assertThat(asString(id)).isEqualTo("0;1;2;3;4");
  }

  private static class A {}

  private static class B {}

  @IndexedType("c")
  private static class Z {}

  private static class D {}
}
