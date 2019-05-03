package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
@NoArgsConstructor
public class RepositoryRoleDto extends HalRepresentation {
  private String name;
  private Collection<String> verbs;

  RepositoryRoleDto(Links links, Embedded embedded) {
    super(links, embedded);
  }
}
