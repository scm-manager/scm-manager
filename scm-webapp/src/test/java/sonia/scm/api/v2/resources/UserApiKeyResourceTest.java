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
