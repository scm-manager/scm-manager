package sonia.scm.api.v2.resources;

import org.mapstruct.Context;
import org.mapstruct.Mapping;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;

public interface ChangesetToChangesetDtoMapper {

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  ChangesetDto map(Changeset changeset, @Context Repository repository);


}
