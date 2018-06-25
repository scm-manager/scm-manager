package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(callSuper = false)
class CollectionDto extends HalRepresentation {

  private int page;
  private int pageTotal;

  CollectionDto(Links links, Embedded embedded) {
    super(links, embedded);
  }
}
