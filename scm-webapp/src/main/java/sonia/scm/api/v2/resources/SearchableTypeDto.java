package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@SuppressWarnings("java:S2160") // we need no equals for dtos
public class SearchableTypeDto extends HalRepresentation {
  private String name;
  private String type;
}
