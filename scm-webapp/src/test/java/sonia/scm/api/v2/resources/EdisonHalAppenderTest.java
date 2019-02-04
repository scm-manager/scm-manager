package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.otto.edison.hal.Links.linkingTo;
import static org.assertj.core.api.Assertions.assertThat;

class EdisonHalAppenderTest {

  private Links.Builder builder;
  private EdisonHalAppender appender;

  @BeforeEach
  void prepare() {
    builder = linkingTo();
    appender = new EdisonHalAppender(builder);
  }

  @Test
  void shouldAppendOneLink() {
    appender.appendOne("self", "https://scm.hitchhiker.com");

    Links links = builder.build();
    assertThat(links.getLinkBy("self").get().getHref()).isEqualTo("https://scm.hitchhiker.com");
  }

  @Test
  void shouldAppendMultipleLinks() {
    appender.arrayBuilder("items")
        .append("one", "http://one")
        .append("two", "http://two")
        .build();

    List<Link> items = builder.build().getLinksBy("items");
    assertThat(items).hasSize(2);
  }

}
