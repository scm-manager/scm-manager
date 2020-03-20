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
