package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;

@Getter
public class IndexDto extends HalRepresentation {

  private final String version;

  IndexDto(String version, Links links) {
    super(links);
    this.version = version;
  }
}
