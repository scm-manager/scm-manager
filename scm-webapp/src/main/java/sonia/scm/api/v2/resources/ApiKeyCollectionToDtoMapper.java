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
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import jakarta.inject.Inject;
import sonia.scm.security.ApiKey;

import java.util.Collection;
import java.util.List;

import static de.otto.edison.hal.Link.link;
import static java.util.stream.Collectors.toList;

public class ApiKeyCollectionToDtoMapper {

  private final ApiKeyToApiKeyDtoMapper apiKeyDtoMapper;
  private final ResourceLinks resourceLinks;

  @Inject
  public ApiKeyCollectionToDtoMapper(ApiKeyToApiKeyDtoMapper apiKeyDtoMapper, ResourceLinks resourceLinks) {
    this.apiKeyDtoMapper = apiKeyDtoMapper;
    this.resourceLinks = resourceLinks;
  }

  public HalRepresentation map(Collection<ApiKey> keys, String user) {
    List<ApiKeyDto> dtos = keys.stream().map(key -> apiKeyDtoMapper.map(key, user)).collect(toList());
    final Links.Builder links = Links.linkingTo()
      .self(resourceLinks.apiKeyCollection().self(user))
      .single(link("create", resourceLinks.apiKeyCollection().create(user)));
    return new HalRepresentation(links.build(), Embedded.embedded("keys", dtos));
  }
}
