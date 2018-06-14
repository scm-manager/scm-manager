package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Data;

@Data
public class GroupCollectionDto extends HalRepresentation {

  private int page;
  private int pageTotal;

  public GroupCollectionDto(Links links, Embedded embedded) {
    super(links, embedded);
  }
}
