package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;

public class IndexDto extends HalRepresentation {

  IndexDto(Links links) {
    super(links);
  }
}
