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
import sonia.scm.admin.UpdateInfo;

import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class UpdateInfoMapper extends HalAppenderMapper {

  @Inject
  private ResourceLinks resourceLinks;

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  public abstract UpdateInfoDto map(UpdateInfo updateInfo);

  @ObjectFactory
  UpdateInfoDto createDto() {
    Links.Builder linksBuilder = linkingTo()
      .self(resourceLinks.adminInfo().updateInfo());

    return new UpdateInfoDto(linksBuilder.build(), Embedded.emptyEmbedded());
  }
}
