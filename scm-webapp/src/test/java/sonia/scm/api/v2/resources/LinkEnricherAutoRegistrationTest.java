package sonia.scm.api.v2.resources;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Java6Assertions.assertThat;

class LinkEnricherAutoRegistrationTest {

  @Test
  void shouldRegisterAllAvailableLinkEnrichers() {
    LinkEnricher one = new One();
    LinkEnricher two = new Two();
    LinkEnricher three = new Three();
    LinkEnricher four = new Four();
    Set<LinkEnricher> enrichers = ImmutableSet.of(one, two, three, four);

    LinkEnricherRegistry registry = new LinkEnricherRegistry();

    LinkEnricherAutoRegistration autoRegistration = new LinkEnricherAutoRegistration(registry, enrichers);
    autoRegistration.contextInitialized(null);

    assertThat(registry.allByType(String.class)).containsOnly(one, two);
    assertThat(registry.allByType(Integer.class)).containsOnly(three);
  }

  @Enrich(String.class)
  public static class One implements LinkEnricher {

    @Override
    public void enrich(LinkEnricherContext context, LinkAppender appender) {

    }
  }

  @Enrich(String.class)
  public static class Two implements LinkEnricher {

    @Override
    public void enrich(LinkEnricherContext context, LinkAppender appender) {

    }
  }

  @Enrich(Integer.class)
  public static class Three implements LinkEnricher {

    @Override
    public void enrich(LinkEnricherContext context, LinkAppender appender) {

    }
  }

  public static class Four implements LinkEnricher {

    @Override
    public void enrich(LinkEnricherContext context, LinkAppender appender) {

    }
  }

}
