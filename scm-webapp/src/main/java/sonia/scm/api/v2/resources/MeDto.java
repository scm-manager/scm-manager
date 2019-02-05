package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MeDto extends HalRepresentation {

  private String name;
  private String displayName;
  private String mail;
  private List<String> groups;

  MeDto(Links links, Embedded embedded) {
    super(links, embedded);
  }
}
