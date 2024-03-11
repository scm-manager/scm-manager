/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
