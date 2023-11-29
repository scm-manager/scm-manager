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

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Links;
import jakarta.inject.Inject;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ObjectFactory;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.Tag;
import sonia.scm.web.EdisonHalAppender;

import java.time.Instant;
import java.util.Optional;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class TagToTagDtoMapper extends HalAppenderMapper {

  @Inject
  private ResourceLinks resourceLinks;

  @Mapping(target = "date", source = "date", qualifiedByName = "mapDate")
  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  @Mapping(target = "signatures")
  public abstract TagDto map(Tag tag, @Context Repository repository);

  @ObjectFactory
  TagDto createDto(@Context Repository repository, Tag tag) {
    Links.Builder linksBuilder = linkingTo()
      .self(resourceLinks.tag().self(repository.getNamespace(), repository.getName(), tag.getName()))
      .single(link("sources", resourceLinks.source().self(repository.getNamespace(), repository.getName(), tag.getRevision())))
      .single(link("changeset", resourceLinks.changeset().self(repository.getNamespace(), repository.getName(), tag.getRevision())));

    if (tag.getDeletable() && RepositoryPermissions.push(repository).isPermitted()) {
      linksBuilder
        .single(link("delete", resourceLinks.tag().delete(repository.getNamespace(), repository.getName(), tag.getName())));
    }

    Embedded.Builder embeddedBuilder = embeddedBuilder();
    applyEnrichers(new EdisonHalAppender(linksBuilder, embeddedBuilder), tag, repository);

    return new TagDto(linksBuilder.build(), embeddedBuilder.build());
  }

  @Named("mapDate")
  Instant map(Optional<Long> value) {
    return value.map(Instant::ofEpochMilli).orElse(null);
  }
}
