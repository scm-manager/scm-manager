package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.Repository;

import javax.inject.Inject;

import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class ModificationsToDtoMapper extends BaseMapper<Modifications, ModificationsDto> {

  @Inject
  private ResourceLinks resourceLinks;

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  public abstract ModificationsDto map(Modifications modifications, @Context Repository repository);

  @AfterMapping
  void appendLinks(@MappingTarget ModificationsDto target, @Context Repository repository) {
    Links.Builder linksBuilder = linkingTo()
      .self(resourceLinks.modifications().self(repository.getNamespace(), repository.getName(), target.getRevision()));
    target.add(linksBuilder.build());
  }
}
