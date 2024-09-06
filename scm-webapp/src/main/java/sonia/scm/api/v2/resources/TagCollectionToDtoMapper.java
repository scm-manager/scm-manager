/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.api.v2.resources;

import com.google.inject.Inject;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;
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

  public HalRepresentation map(Collection<Tag> tags, Repository repository) {
    return new HalRepresentation(createLinks(repository.getNamespace(), repository.getName()), embedDtos(getTagDtoList(tags, repository)));
  }

  public HalRepresentation map(Collection<Tag> tags, Repository repository, String changeset) {
    return new HalRepresentation(createLinks(repository.getNamespace(), repository.getName(), changeset), embedDtos(getTagDtoList(tags, repository)));
  }

  public List<TagDto> getTagDtoList(Collection<Tag> tags, Repository repository) {
    return tags.stream().map(tag -> tagToTagDtoMapper.map(tag, repository)).collect(toList());
  }

  public List<TagDto> getMinimalEmbeddedTagDtoList(String namespace, String name, Collection<String> tags) {
    return tags.stream()
      .map(tag -> {
        Links links = linkingTo().self(resourceLinks.tag().self(namespace, name, tag)).build();
        TagDto dto = new TagDto(links);
        dto.setName(tag);
        return dto;
      })
      .collect(toList());
  }

  private Links createLinks(String namespace, String name) {
    return
      linkingTo()
        .self(resourceLinks.tag().all(namespace, name))
        .build();
  }

  private Links createLinks(String namespace, String name, String changeset) {
    return
      linkingTo()
        .self(resourceLinks.tag().getForChangeset(namespace, name, changeset))
        .build();
  }

  private Embedded embedDtos(List<TagDto> dtos) {
    return embeddedBuilder()
      .with("tags", dtos)
      .build();
  }
}
