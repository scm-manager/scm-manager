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
