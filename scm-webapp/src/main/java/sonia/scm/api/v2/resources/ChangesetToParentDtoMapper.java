package sonia.scm.api.v2.resources;


import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;

import javax.inject.Inject;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class ChangesetToParentDtoMapper extends BaseMapper<Changeset, ParentChangesetDto> {

  @Inject
  private ResourceLinks resourceLinks;

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  public abstract ParentChangesetDto map(Changeset changeset, @Context Repository repository);


  @AfterMapping
  void appendLinks(@MappingTarget ParentChangesetDto target, @Context Repository repository) {
    String namespace = repository.getNamespace();
    String name = repository.getName();
    Links.Builder linksBuilder = linkingTo()
      .self(resourceLinks.changeset().self(repository.getNamespace(), repository.getName(), target.getId()))
      .single(link("diff", resourceLinks.diff().self(namespace, name, target.getId())));
    target.add(linksBuilder.build());
  }

}
