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

package sonia.scm.util;

import org.junit.jupiter.api.Test;

import java.util.Comparator;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ComparablesTest {

  @Test
  void shouldCompare() {
    One a = new One("a");
    One b = new One("b");

    Comparator<One> comparable = Comparables.comparator(One.class, "value");
    assertThat(comparable.compare(a, b)).isEqualTo(-1);
  }

  @Test
  void shouldCompareWithNullValues() {
    One a = new One("a");
    One b = new One(null);

    Comparator<One> comparable = Comparables.comparator(One.class, "value");
    assertThat(comparable.compare(a, b)).isEqualTo(1);
    assertThat(comparable.compare(b, a)).isEqualTo(-1);
  }

  @Test
  void shouldThrowAnExceptionForNonExistingField() {
    assertThrows(IllegalArgumentException.class, () -> Comparables.comparator(One.class, "awesome"));
  }

  @Test
  void shouldThrowAnExceptionForNonComparableField() {
    assertThrows(IllegalArgumentException.class, () -> Comparables.comparator(One.class, "nonComparable"));
  }

  @Test
  void shouldThrowAnExceptionIfTheFieldHasNoGetter() {
    assertThrows(IllegalArgumentException.class, () -> Comparables.comparator(One.class, "incredible"));
  }

  private static class One {

    private String value;
    private String incredible;
    private NonComparable nonComparable;

    One(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    public NonComparable getNonComparable() {
      return nonComparable;
    }
  }

  private static class NonComparable {}

}
