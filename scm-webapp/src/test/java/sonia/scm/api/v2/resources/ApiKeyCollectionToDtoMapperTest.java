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

    assertThat(halRepresentation.getLinks().getLinkBy("self").isPresent()).isTrue();
    assertThat(halRepresentation.getLinks().getLinkBy("create").isPresent()).isTrue();
  }

}
