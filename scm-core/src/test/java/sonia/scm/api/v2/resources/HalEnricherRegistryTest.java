package sonia.scm.api.v2.resources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HalEnricherRegistryTest {

  private HalEnricherRegistry registry;

  @BeforeEach
  void setUpObjectUnderTest() {
    registry = new HalEnricherRegistry();
  }

  @Test
  void shouldRegisterTheEnricher() {
    SampleHalEnricher enricher = new SampleHalEnricher();
    registry.register(String.class, enricher);

    Iterable<HalEnricher> enrichers = registry.allByType(String.class);
    assertThat(enrichers).containsOnly(enricher);
  }

  @Test
  void shouldRegisterMultipleEnrichers() {
    SampleHalEnricher one = new SampleHalEnricher();
    registry.register(String.class, one);

    SampleHalEnricher two = new SampleHalEnricher();
    registry.register(String.class, two);

    Iterable<HalEnricher> enrichers = registry.allByType(String.class);
    assertThat(enrichers).containsOnly(one, two);
  }

  @Test
  void shouldRegisterEnrichersForDifferentTypes() {
    SampleHalEnricher one = new SampleHalEnricher();
    registry.register(String.class, one);

    SampleHalEnricher two = new SampleHalEnricher();
    registry.register(Integer.class, two);

    Iterable<HalEnricher> enrichers = registry.allByType(String.class);
    assertThat(enrichers).containsOnly(one);

    enrichers = registry.allByType(Integer.class);
    assertThat(enrichers).containsOnly(two);
  }

  private static class SampleHalEnricher implements HalEnricher {
    @Override
    public void enrich(HalEnricherContext context, HalAppender appender) {

    }
  }

}
