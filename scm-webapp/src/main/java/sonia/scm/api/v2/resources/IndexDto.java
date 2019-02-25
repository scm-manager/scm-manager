package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;

@Getter
public class IndexDto extends HalRepresentation {

  private final String version;

  IndexDto(Links links, Embedded embedded, String version) {
    super(links, embedded);
    this.version = version;
  }
}
