package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Links.linkingTo;
import static org.assertj.core.api.Assertions.assertThat;

class EdisonHalAppenderTest {

  private Links.Builder linksBuilder;
  private Embedded.Builder embeddedBuilder;
  private EdisonHalAppender appender;

  @BeforeEach
  void prepare() {
    linksBuilder = linkingTo();
    embeddedBuilder = embeddedBuilder();
    appender = new EdisonHalAppender(linksBuilder, embeddedBuilder);
  }

  @Test
  void shouldAppendOneLink() {
    appender.appendLink("self", "https://scm.hitchhiker.com");

    Links links = linksBuilder.build();
    assertThat(links.getLinkBy("self").get().getHref()).isEqualTo("https://scm.hitchhiker.com");
  }

  @Test
  void shouldAppendMultipleLinks() {
    appender.linkArrayBuilder("items")
        .append("one", "http://one")
        .append("two", "http://two")
        .build();

    List<Link> items = linksBuilder.build().getLinksBy("items");
    assertThat(items).hasSize(2);
  }

  @Test
  void shouldAppendEmbedded() {
    HalRepresentation one = new HalRepresentation();
    appender.appendEmbedded("one", one);

    HalRepresentation two = new HalRepresentation();
    appender.appendEmbedded("two", new HalRepresentation());

    Embedded embedded = embeddedBuilder.build();
    assertThat(embedded.getItemsBy("one")).containsOnly(one);
    assertThat(embedded.getItemsBy("two")).containsOnly(two);
  }

}
