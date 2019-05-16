package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;

import java.util.Collection;

public class RepositoryVerbsDto extends HalRepresentation {
  private final Collection<String> verbs;

  public RepositoryVerbsDto(Links links, Collection<String> verbs) {
    super(links);
    this.verbs = verbs;
  }

  public Collection<String> getVerbs() {
    return verbs;
  }
}
