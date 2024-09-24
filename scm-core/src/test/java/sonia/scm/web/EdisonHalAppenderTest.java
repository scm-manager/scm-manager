/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
