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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import jakarta.servlet.http.HttpServletResponse;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.admin.ScmConfigurationStore;
import sonia.scm.config.SecureKeyService;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.NamespaceStrategyValidator;
import sonia.scm.store.InMemoryConfigurationStoreFactory;
import sonia.scm.util.JsonMerger;
import sonia.scm.web.RestDispatcher;
import sonia.scm.web.VndMediaType;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(ShiroExtension.class)
@ExtendWith(MockitoExtension.class)
@SubjectAware(
  value = "trillian"
)
class ConfigResourceTest {

  private final RestDispatcher dispatcher = new RestDispatcher();

  private final URI baseUri = URI.create("/");
  @SuppressWarnings("unused") // Is injected
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @Mock
  private NamespaceStrategyValidator namespaceStrategyValidator;

  private final JsonMerger jsonMerger = new JsonMerger(new ObjectMapper());

  @InjectMocks
  private ConfigDtoToScmConfigurationMapperImpl dtoToConfigMapper;
  @InjectMocks
  private ScmConfigurationToConfigDtoMapperImpl configToDtoMapper;
  @Mock
  private SecureKeyService secureKeyService;

  @BeforeEach
  void prepareEnvironment() {
    ConfigResource configResource = new ConfigResource(
      new ScmConfigurationStore(
        new InMemoryConfigurationStoreFactory(),
        new ScmConfiguration()
      ),
      dtoToConfigMapper,
      configToDtoMapper,
      namespaceStrategyValidator,
      jsonMerger,
      secureKeyService
    );

    dispatcher.addSingletonResource(configResource);
  }

  @Test
  @SubjectAware(
    permissions = "configuration:read:global"
  )
  void shouldGetGlobalConfig() throws URISyntaxException, UnsupportedEncodingException {
    MockHttpRequest request = MockHttpRequest.get("/" + ConfigResource.CONFIG_PATH_V2);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getContentAsString()).contains("\"proxyServer\":\"proxy.mydomain.com\"");
    assertThat(response.getContentAsString()).contains("\"namespaceStrategy\":\"UsernameNamespaceStrategy\"");
    assertThat(response.getContentAsString()).contains("\"self\":{\"href\":\"/v2/config");
    assertThat(response.getContentAsString()).doesNotContain("\"update\":{\"href\":\"/v2/config");
  }

  @Test
  void shouldNotGetConfigWhenNotAuthorized() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/" + ConfigResource.CONFIG_PATH_V2);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
  }

  @Test
  @SubjectAware(
    permissions = "configuration:read,write:global"
  )
  void shouldUpdateConfig() throws URISyntaxException, IOException {
    MockHttpRequest request = put();

    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NO_CONTENT);

    request = MockHttpRequest.get("/" + ConfigResource.CONFIG_PATH_V2);
    response = new MockHttpResponse();
    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    // Should overwrite old realm description with null
    assertThat(response.getContentAsString()).contains("\"realmDescription\":null");
    assertThat(response.getContentAsString()).contains("\"proxyPassword\":\"newPassword\"");
    assertThat(response.getContentAsString()).contains("\"self\":{\"href\":\"/v2/config");
    assertThat(response.getContentAsString()).contains("\"update\":{\"href\":\"/v2/config");
  }

  @Test
  void shouldNotUpdateConfigWhenNotAuthorized() throws URISyntaxException, IOException {
    MockHttpRequest request = put();
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
  }

  @Test
  @SubjectAware(
    permissions = "configuration:write:global"
  )
  void shouldValidateNamespaceStrategy() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.put("/" + ConfigResource.CONFIG_PATH_V2)
      .contentType(VndMediaType.CONFIG)
      .content("{ \"namespaceStrategy\": \"AwesomeStrategy\" }".getBytes(StandardCharsets.UTF_8));

    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NO_CONTENT);
    verify(namespaceStrategyValidator).check("AwesomeStrategy");
  }

  @Test
  @SubjectAware(
    permissions = "configuration:read,write:global"
  )
  void shouldUpdateConfigPartially() throws URISyntaxException, IOException {
    MockHttpRequest request = patch("{ \"proxyPort\":\"1337\", \"proxyPassword\":null }");

    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NO_CONTENT);

    request = MockHttpRequest.get("/" + ConfigResource.CONFIG_PATH_V2);
    response = new MockHttpResponse();
    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    // Should not change old realm description
    assertThat(response.getContentAsString()).contains("\"realmDescription\":\"SONIA :: SCM Manager\"");
    assertThat(response.getContentAsString()).contains("\"proxyPassword\":null");
    assertThat(response.getContentAsString()).contains("\"proxyPort\":1337");
    assertThat(response.getContentAsString()).contains("\"self\":{\"href\":\"/v2/config");
    assertThat(response.getContentAsString()).contains("\"update\":{\"href\":\"/v2/config");
  }

  @Test
  void shouldNotUpdateConfigPartiallyIfNotAuthorized() throws URISyntaxException {
    MockHttpRequest request = patch("{ \"proxyPort\":\"1337\", \"proxyPassword\":\"hitchhiker\" }");

    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
  }

  private MockHttpRequest put() throws IOException, URISyntaxException {
    URL url = Resources.getResource("sonia/scm/api/v2/config-test-update.json");
    byte[] configJson = Resources.toByteArray(url);
    return MockHttpRequest.put("/" + ConfigResource.CONFIG_PATH_V2)
      .contentType(VndMediaType.CONFIG)
      .content(configJson);
  }

  private MockHttpRequest patch(String json) throws URISyntaxException {
    return MockHttpRequest.patch("/" + ConfigResource.CONFIG_PATH_V2)
      .contentType(VndMediaType.CONFIG)
      .content(json.getBytes());
  }
}
