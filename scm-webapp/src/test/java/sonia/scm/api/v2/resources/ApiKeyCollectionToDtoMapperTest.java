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

import de.otto.edison.hal.HalRepresentation;
import org.junit.Test;
import sonia.scm.security.ApiKey;

import java.net.URI;
import java.util.Collections;

import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ApiKeyCollectionToDtoMapperTest {

  public static final ApiKey API_KEY = new ApiKey("1", "key 1", "READ", now());

  private final ApiKeyToApiKeyDtoMapper apiKeyDtoMapper = mock(ApiKeyToApiKeyDtoMapper.class);
  private final ApiKeyCollectionToDtoMapper apiKeyCollectionToDtoMapper = new ApiKeyCollectionToDtoMapper(apiKeyDtoMapper, ResourceLinksMock.createMock(URI.create("/")));

  @Test
  public void shouldMapCollection() {
    ApiKeyDto expectedApiKeyDto = new ApiKeyDto();
    when(apiKeyDtoMapper.map(API_KEY, "user")).thenReturn(expectedApiKeyDto);

    HalRepresentation halRepresentation = apiKeyCollectionToDtoMapper.map(Collections.singletonList(API_KEY), "user");

    assertThat(halRepresentation.getEmbedded().hasItem("keys")).isTrue();
    assertThat(halRepresentation.getEmbedded().getItemsBy("keys")).containsExactly(expectedApiKeyDto);
  }

  @Test
  public void shouldEmbedLinks() {
    ApiKeyDto expectedApiKeyDto = new ApiKeyDto();
    when(apiKeyDtoMapper.map(API_KEY, "user")).thenReturn(expectedApiKeyDto);

    HalRepresentation halRepresentation = apiKeyCollectionToDtoMapper.map(Collections.singletonList(API_KEY), "user");

    assertThat(halRepresentation.getLinks().getLinkBy("self")).isPresent();
    assertThat(halRepresentation.getLinks().getLinkBy("create")).isPresent();
  }

}
