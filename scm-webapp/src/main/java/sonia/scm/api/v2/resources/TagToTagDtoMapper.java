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
