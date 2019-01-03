package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;

class EdisonLinkAppender implements LinkAppender {

  private final Links.Builder builder;

  EdisonLinkAppender(Links.Builder builder) {
    this.builder = builder;
  }

  @Override
  public void appendOne(String rel, String href) {
    builder.single(Link.link(rel, href));
  }
}
