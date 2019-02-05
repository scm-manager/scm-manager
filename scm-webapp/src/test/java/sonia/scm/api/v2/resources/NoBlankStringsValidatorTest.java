package sonia.scm.api.v2.resources;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NoBlankStringsValidatorTest {

  @Test
  void shouldAcceptNonEmptyElements() {
    assertTrue(new NoBlankStringsValidator().isValid(Arrays.asList("not", "empty"), null));
  }

  @Test
  void shouldFailForEmptyElements() {
    assertFalse(new NoBlankStringsValidator().isValid(Arrays.asList("one", "", "three"), null));
  }

  @Test
  void shouldAcceptEmptyList() {
    assertTrue(new NoBlankStringsValidator().isValid(emptySet(), null));
  }
}
