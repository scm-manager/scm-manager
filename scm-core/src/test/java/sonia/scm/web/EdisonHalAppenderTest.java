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
    
package sonia.scm.web;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sonia.scm.web.EdisonHalAppender;

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
