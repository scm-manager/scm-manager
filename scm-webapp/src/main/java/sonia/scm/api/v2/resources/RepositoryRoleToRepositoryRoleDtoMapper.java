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
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import sonia.scm.repository.RepositoryRole;
import sonia.scm.repository.RepositoryRolePermissions;
import sonia.scm.web.EdisonHalAppender;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

// Mapstruct does not support parameterized (i.e. non-default) constructors. Thus, we need to use field injection.
@SuppressWarnings("squid:S3306")
@Mapper
public abstract class RepositoryRoleToRepositoryRoleDtoMapper extends BaseMapper<RepositoryRole, RepositoryRoleDto> {

  @Inject
  private ResourceLinks resourceLinks;

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  @Override
  public abstract RepositoryRoleDto map(RepositoryRole modelObject);

  @ObjectFactory
  RepositoryRoleDto createDto(RepositoryRole repositoryRole) {
    Links.Builder linksBuilder = linkingTo().self(resourceLinks.repositoryRole().self(repositoryRole.getName()));
    if (!"system".equals(repositoryRole.getType()) && RepositoryRolePermissions.write().isPermitted()) {
      linksBuilder.single(link("delete", resourceLinks.repositoryRole().delete(repositoryRole.getName())));
      linksBuilder.single(link("update", resourceLinks.repositoryRole().update(repositoryRole.getName())));
    }

    Embedded.Builder embeddedBuilder = embeddedBuilder();
    applyEnrichers(new EdisonHalAppender(linksBuilder, embeddedBuilder), repositoryRole);

    return new RepositoryRoleDto(linksBuilder.build(), embeddedBuilder.build());
  }

}
