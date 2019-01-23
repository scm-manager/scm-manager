package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import sonia.scm.security.RepositoryRole;

import java.util.Collection;

public class AvailableRepositoryPermissionsDto extends HalRepresentation {
  private final Collection<String> availableVerbs;
  private final Collection<RepositoryRole> availableRoles;

  public AvailableRepositoryPermissionsDto(Collection<String> availableVerbs, Collection<RepositoryRole> availableRoles) {
    this.availableVerbs = availableVerbs;
    this.availableRoles = availableRoles;
  }

  public Collection<String> getAvailableVerbs() {
    return availableVerbs;
  }

  public Collection<RepositoryRole> getAvailableRoles() {
    return availableRoles;
  }

  @Override
  @SuppressWarnings("squid:S1185") // We want to have this method available in this package
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }
}
