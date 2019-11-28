package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Links;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Tag;

import javax.inject.Inject;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class TagToTagDtoMapper extends HalAppenderMapper {

  @Inject
  private ResourceLinks resourceLinks;

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  public abstract TagDto map(Tag tag, @Context NamespaceAndName namespaceAndName);

  @ObjectFactory
  TagDto createDto(@Context NamespaceAndName namespaceAndName, Tag tag) {
    Links.Builder linksBuilder = linkingTo()
      .self(resourceLinks.tag().self(namespaceAndName.getNamespace(), namespaceAndName.getName(), tag.getName()))
      .single(link("sources", resourceLinks.source().self(namespaceAndName.getNamespace(), namespaceAndName.getName(), tag.getRevision())))
      .single(link("changeset", resourceLinks.changeset().self(namespaceAndName.getNamespace(), namespaceAndName.getName(), tag.getRevision())));

    Embedded.Builder embeddedBuilder = embeddedBuilder();
    applyEnrichers(new EdisonHalAppender(linksBuilder, embeddedBuilder), tag, namespaceAndName);

    return new TagDto(linksBuilder.build(), embeddedBuilder.build());
  }
}
