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
