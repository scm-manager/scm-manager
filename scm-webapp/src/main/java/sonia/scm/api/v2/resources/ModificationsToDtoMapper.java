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

import de.otto.edison.hal.Links;
import jakarta.inject.Inject;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sonia.scm.repository.Added;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.Modified;
import sonia.scm.repository.Removed;
import sonia.scm.repository.Renamed;
import sonia.scm.repository.Repository;

import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class ModificationsToDtoMapper {

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

  String map(Added added) {
    return added.getPath();
  }

  String map(Removed removed) {
    return removed.getPath();
  }

  String map(Modified modified) {
    return modified.getPath();
  }

  abstract ModificationsDto.RenamedDto map(Renamed renamed);
}
