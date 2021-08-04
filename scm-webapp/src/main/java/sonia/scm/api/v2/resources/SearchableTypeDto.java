package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
@NoArgsConstructor
@SuppressWarnings("java:S2160") // we need no equals for dtos
public class SearchableTypeDto extends HalRepresentation {
  private String name;
  private String type;
  private Collection<SearchableFieldDto> fields;
}
