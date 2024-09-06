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
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.security.AnonymousMode;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

// Mapstruct does not support parameterized (i.e. non-default) constructors. Thus, we need to use field injection.
@SuppressWarnings("squid:S3306")
@Mapper
public abstract class ScmConfigurationToConfigDtoMapper extends BaseMapper<ScmConfiguration, ConfigDto> {

  @Inject
  private ResourceLinks resourceLinks;

  @Mapping(target = "anonymousAccessEnabled", source = "anonymousMode", qualifiedByName = "mapAnonymousAccess")
  @Mapping(target = "attributes", ignore = true)
  public abstract ConfigDto map(ScmConfiguration scmConfiguration);

  @Named("mapAnonymousAccess")
  boolean mapAnonymousAccess(AnonymousMode anonymousMode) {
    return anonymousMode != AnonymousMode.OFF;
  }

  @AfterMapping
  void appendLinks(ScmConfiguration config, @MappingTarget ConfigDto target) {
    Links.Builder linksBuilder = linkingTo().self(resourceLinks.config().self());
    if (ConfigurationPermissions.write(config).isPermitted()) {
      linksBuilder.single(link("update", resourceLinks.config().update()));
    }
    target.add(linksBuilder.build());
  }

}
