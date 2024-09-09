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

import com.google.common.annotations.VisibleForTesting;
import de.otto.edison.hal.Links;
import jakarta.inject.Inject;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import sonia.scm.repository.HgRepositoryConfig;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class HgRepositoryConfigMapper {

  @Inject
  private HgConfigLinks links;

  @VisibleForTesting
  void setLinks(HgConfigLinks links) {
    this.links = links;
  }

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  abstract HgRepositoryConfigDto map(@Context Repository repository, HgRepositoryConfig config);
  abstract HgRepositoryConfig map(HgRepositoryConfigDto dto);

  @ObjectFactory
  HgRepositoryConfigDto createDto(@Context Repository repository) {
    HgConfigLinks.ConfigLinks configLinks = this.links.repository(repository);
    Links.Builder linksBuilder = linkingTo().self(configLinks.get());
    if (RepositoryPermissions.custom("hg", repository).isPermitted()) {
      linksBuilder.single(link("update", configLinks.update()));
    }
    return new HgRepositoryConfigDto(linksBuilder.build());
  }
}
