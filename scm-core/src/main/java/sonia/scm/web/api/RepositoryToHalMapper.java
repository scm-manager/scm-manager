package sonia.scm.web.api;

import de.otto.edison.hal.HalRepresentation;
import sonia.scm.repository.Repository;

public interface RepositoryToHalMapper {
  HalRepresentation map(Repository repository);
}
