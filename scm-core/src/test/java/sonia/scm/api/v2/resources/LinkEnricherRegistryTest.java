package sonia.scm.api.v2.resources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LinkEnricherRegistryTest {

  private LinkEnricherRegistry registry;

  @BeforeEach
  void setUpObjectUnderTest() {
    registry = new LinkEnricherRegistry();
  }

  @Test
  void shouldRegisterTheEnricher() {
    SampleLinkEnricher enricher = new SampleLinkEnricher();
    registry.register(String.class, enricher);

    Iterable<LinkEnricher> enrichers = registry.allByType(String.class);
    assertThat(enrichers).containsOnly(enricher);
  }

  @Test
  void shouldRegisterMultipleEnrichers() {
    SampleLinkEnricher one = new SampleLinkEnricher();
    registry.register(String.class, one);

    SampleLinkEnricher two = new SampleLinkEnricher();
    registry.register(String.class, two);

    Iterable<LinkEnricher> enrichers = registry.allByType(String.class);
    assertThat(enrichers).containsOnly(one, two);
  }

  @Test
  void shouldRegisterEnrichersForDifferentTypes() {
    SampleLinkEnricher one = new SampleLinkEnricher();
    registry.register(String.class, one);

    SampleLinkEnricher two = new SampleLinkEnricher();
    registry.register(Integer.class, two);

    Iterable<LinkEnricher> enrichers = registry.allByType(String.class);
    assertThat(enrichers).containsOnly(one);

    enrichers = registry.allByType(Integer.class);
    assertThat(enrichers).containsOnly(two);
  }

  private static class SampleLinkEnricher implements LinkEnricher {
    @Override
    public void enrich(LinkEnricherContext context, LinkAppender appender) {

    }
  }

}
