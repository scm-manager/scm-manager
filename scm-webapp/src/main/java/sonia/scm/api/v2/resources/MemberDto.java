package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class MemberDto extends HalRepresentation {
  private String name;

  @Override
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }
}
