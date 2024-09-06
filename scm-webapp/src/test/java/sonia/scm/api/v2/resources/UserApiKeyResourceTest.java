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

import com.google.inject.util.Providers;
import jakarta.servlet.http.HttpServletResponse;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import sonia.scm.security.ApiKey;
import sonia.scm.security.ApiKeyService;
import sonia.scm.web.RestDispatcher;
import sonia.scm.web.VndMediaType;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import static java.time.Instant.now;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class UserApiKeyResourceTest {

  private final RestDispatcher dispatcher = new RestDispatcher();
  private final MockHttpResponse response = new MockHttpResponse();
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(URI.create("/"));

  @Mock
  private ApiKeyService apiKeyService;

  @InjectMocks
  private ApiKeyToApiKeyDtoMapperImpl apiKeyMapper;

  @Before
  public void prepareEnvironment() {
    initMocks(this);
    ApiKeyCollectionToDtoMapper apiKeyCollectionMapper = new ApiKeyCollectionToDtoMapper(apiKeyMapper, resourceLinks);
    UserApiKeyResource userApiKeyResource = new UserApiKeyResource(apiKeyService, apiKeyCollectionMapper, apiKeyMapper, resourceLinks);
    UserRootResource userRootResource = new UserRootResource(null, null, Providers.of(userApiKeyResource));
    dispatcher.addSingletonResource(userRootResource);
  }

  @Test
  public void shouldGetAllApiKeys() throws URISyntaxException, UnsupportedEncodingException {
    when(apiKeyService.getKeys("trillian"))
      .thenReturn(asList(
        new ApiKey("1", "key 1", "READ", now()),
        new ApiKey("2", "key 2", "WRITE", now())));

    MockHttpRequest request = MockHttpRequest.get("/" + UserRootResource.USERS_PATH_V2 + "trillian/api_keys");
    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());

    assertThat(response.getContentAsString()).contains("\"displayName\":\"key 1\",\"permissionRole\":\"READ\"");
    assertThat(response.getContentAsString()).contains("\"displayName\":\"key 2\",\"permissionRole\":\"WRITE\"");
    assertThat(response.getContentAsString()).contains("\"self\":{\"href\":\"/v2/users/trillian/api_keys\"}");
    assertThat(response.getContentAsString()).contains("\"create\":{\"href\":\"/v2/users/trillian/api_keys\"}");
  }

  @Test
  public void shouldGetSingleApiKey() throws URISyntaxException, UnsupportedEncodingException {
    when(apiKeyService.getKeys("trillian"))
      .thenReturn(asList(
        new ApiKey("1", "key 1", "READ", now()),
        new ApiKey("2", "key 2", "WRITE", now())));

    MockHttpRequest request = MockHttpRequest.get("/" + UserRootResource.USERS_PATH_V2 + "trillian/api_keys/1");
    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());

    assertThat(response.getContentAsString()).contains("\"displayName\":\"key 1\"");
    assertThat(response.getContentAsString()).contains("\"permissionRole\":\"READ\"");
    assertThat(response.getContentAsString()).contains("\"self\":{\"href\":\"/v2/users/trillian/api_keys/1\"}");
    assertThat(response.getContentAsString()).contains("\"delete\":{\"href\":\"/v2/users/trillian/api_keys/1\"}");
  }

  @Test
  public void shouldCreateNewApiKey() throws URISyntaxException, UnsupportedEncodingException {
    when(apiKeyService.createNewKey("trillian", "guide", "READ")).thenReturn(new ApiKeyService.CreationResult("abc", "1"));

    final MockHttpRequest request = MockHttpRequest
      .post("/" + UserRootResource.USERS_PATH_V2 + "trillian/api_keys/")
      .contentType(VndMediaType.API_KEY)
      .content("{\"displayName\":\"guide\",\"permissionRole\":\"READ\"}".getBytes());

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(201);
    assertThat(response.getContentAsString()).isEqualTo("abc");
    assertThat(response.getOutputHeaders().get("Location")).containsExactly(URI.create("/v2/users/trillian/api_keys/1"));
  }

  @Test
  public void shouldIgnoreInvalidNewApiKey() throws URISyntaxException {
    when(apiKeyService.createNewKey("trillian", "guide", "READ")).thenReturn(new ApiKeyService.CreationResult("abc", "1"));

    final MockHttpRequest request = MockHttpRequest
      .post("/" + UserRootResource.USERS_PATH_V2 + "trillian/api_keys/")
      .contentType(VndMediaType.API_KEY)
      .content("{\"displayName\":\"guide\",\"pemissionRole\":\"\"}".getBytes());

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(400);
  }

  @Test
  public void shouldDeleteExistingApiKey() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.delete("/" + UserRootResource.USERS_PATH_V2 + "trillian/api_keys/1");
    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(204);
    verify(apiKeyService).remove("trillian", "1");
  }
}
