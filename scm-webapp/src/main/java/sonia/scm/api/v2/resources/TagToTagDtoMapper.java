package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Tag;

import javax.inject.Inject;

import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class TagToTagDtoMapper {

  @Inject
  private ResourceLinks resourceLinks;

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  public abstract TagDto map(Tag tag, @Context NamespaceAndName namespaceAndName);

  @AfterMapping
  void appendLinks(@MappingTarget TagDto target, @Context NamespaceAndName namespaceAndName) {
    Links.Builder linksBuilder = linkingTo()
      .self(resourceLinks.tag().self(namespaceAndName.getNamespace(), namespaceAndName.getName(), target.getName()));
    target.add(linksBuilder.build());
  }
}
