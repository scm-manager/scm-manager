package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReducedObjectModelDto extends HalRepresentation {

  private String id;

  private String displayName;
}
