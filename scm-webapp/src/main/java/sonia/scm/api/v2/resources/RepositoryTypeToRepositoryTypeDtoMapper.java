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
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import jakarta.inject.Inject;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.RepositoryType;
import sonia.scm.repository.api.Command;
import sonia.scm.web.EdisonHalAppender;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class RepositoryTypeToRepositoryTypeDtoMapper extends BaseMapper<RepositoryType, RepositoryTypeDto> {

  private static final String REL_IMPORT = "import";

  @Inject
  private ResourceLinks resourceLinks;

  @ObjectFactory
  RepositoryTypeDto create(RepositoryType repositoryType) {
    Links.Builder linksBuilder = linkingTo().self(resourceLinks.repositoryType().self(repositoryType.getName()));

    if (RepositoryPermissions.create().isPermitted()) {
      if (repositoryType.getSupportedCommands().contains(Command.PULL)) {
        linksBuilder.array(Link.linkBuilder(REL_IMPORT, resourceLinks.repository().importFromUrl(repositoryType.getName())).withName("url").build());
      }
      if (repositoryType.getSupportedCommands().contains(Command.UNBUNDLE)) {
        linksBuilder.array(Link.linkBuilder(REL_IMPORT, resourceLinks.repository().importFromBundle(repositoryType.getName())).withName("bundle").build());
        linksBuilder.array(Link.linkBuilder(REL_IMPORT, resourceLinks.repository().fullImport(repositoryType.getName())).withName("fullImport").build());
      }
    }

    Embedded.Builder embeddedBuilder = embeddedBuilder();

    applyEnrichers(new EdisonHalAppender(linksBuilder, embeddedBuilder), repositoryType);

    return new RepositoryTypeDto(linksBuilder.build(), embeddedBuilder.build());
  }

}
