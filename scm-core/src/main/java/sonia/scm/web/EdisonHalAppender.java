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
import sonia.scm.api.v2.resources.HalAppender;

import java.util.ArrayList;
import java.util.List;

public final class EdisonHalAppender implements HalAppender {

  private final Links.Builder linkBuilder;
  private final Embedded.Builder embeddedBuilder;

  public EdisonHalAppender(Links.Builder linkBuilder, Embedded.Builder embeddedBuilder) {
    this.linkBuilder = linkBuilder;
    this.embeddedBuilder = embeddedBuilder;
  }

  @Override
  public void appendLink(String rel, String href) {
    linkBuilder.single(Link.link(rel, href));
  }

  @Override
  public LinkArrayBuilder linkArrayBuilder(String rel) {
    return new EdisonLinkArrayBuilder(linkBuilder, rel);
  }

  @Override
  public void appendEmbedded(String rel, HalRepresentation embedded) {
    embeddedBuilder.with(rel, embedded);
  }

  @Override
  public void appendEmbedded(String rel, List<HalRepresentation> embedded) {
    embeddedBuilder.with(rel, embedded);
  }

  private static class EdisonLinkArrayBuilder implements LinkArrayBuilder {

    private final Links.Builder builder;
    private final String rel;
    private final List<Link> linkArray = new ArrayList<>();

    private EdisonLinkArrayBuilder(Links.Builder builder, String rel) {
      this.builder = builder;
      this.rel = rel;
    }

    @Override
    public LinkArrayBuilder append(String name, String href) {
      linkArray.add(Link.linkBuilder(rel, href).withName(name).build());
      return this;
    }

    @Override
    public void build() {
      builder.array(linkArray);
    }
  }
}
