package sonia.scm.api.v2.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

class HalEnricherContextTest {

  @Test
  void shouldCreateContextFromSingleObject() {
    HalEnricherContext context = HalEnricherContext.of("hello");
    assertThat(context.oneByType(String.class)).contains("hello");
  }

  @Test
  void shouldCreateContextFromMultipleObjects() {
    HalEnricherContext context = HalEnricherContext.of("hello", Integer.valueOf(42), Long.valueOf(21L));
    assertThat(context.oneByType(String.class)).contains("hello");
    assertThat(context.oneByType(Integer.class)).contains(42);
    assertThat(context.oneByType(Long.class)).contains(21L);
  }

  @Test
  void shouldReturnEmptyOptionalForUnknownTypes() {
    HalEnricherContext context = HalEnricherContext.of();
    assertThat(context.oneByType(String.class)).isNotPresent();
  }

  @Test
  void shouldReturnRequiredObject() {
    HalEnricherContext context = HalEnricherContext.of("hello");
    assertThat(context.oneRequireByType(String.class)).isEqualTo("hello");
  }

  @Test
  void shouldThrowAnNoSuchElementExceptionForUnknownTypes() {
    HalEnricherContext context = HalEnricherContext.of();
    assertThrows(NoSuchElementException.class, () -> context.oneRequireByType(String.class));
  }

}
