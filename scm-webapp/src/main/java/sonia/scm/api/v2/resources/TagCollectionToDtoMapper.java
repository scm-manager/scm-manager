package sonia.scm.api.v2.resources;

import com.google.inject.Inject;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Tag;

import java.util.Collection;
import java.util.List;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Links.linkingTo;
import static java.util.stream.Collectors.toList;

public class TagCollectionToDtoMapper {


  private final ResourceLinks resourceLinks;
  private final TagToTagDtoMapper tagToTagDtoMapper;

  @Inject
  public TagCollectionToDtoMapper(ResourceLinks resourceLinks, TagToTagDtoMapper tagToTagDtoMapper) {
    this.resourceLinks = resourceLinks;
    this.tagToTagDtoMapper = tagToTagDtoMapper;
  }

  public HalRepresentation map(String namespace, String name, Collection<Tag> tags) {
    return new HalRepresentation(createLinks(namespace, name), embedDtos(getTagDtoList(namespace, name, tags)));
  }

  public List<TagDto> getTagDtoList(String namespace, String name, Collection<Tag> tags) {
    return tags.stream().map(tag -> tagToTagDtoMapper.map(tag, new NamespaceAndName(namespace, name))).collect(toList());
  }

  private Links createLinks(String namespace, String name) {
    return
      linkingTo()
        .self(resourceLinks.tag().all(namespace, name))
        .build();
  }

  private Embedded embedDtos(List<TagDto> dtos) {
    return embeddedBuilder()
      .with("tags", dtos)
      .build();
  }
}
