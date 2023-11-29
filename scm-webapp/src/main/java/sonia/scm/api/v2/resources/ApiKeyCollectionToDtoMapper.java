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
