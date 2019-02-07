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
