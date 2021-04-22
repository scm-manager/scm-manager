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

import com.google.common.io.Resources;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.NamespaceStrategyValidator;
import sonia.scm.web.RestDispatcher;
import sonia.scm.web.VndMediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ConfigResourceTest {

  private final RestDispatcher dispatcher = new RestDispatcher();

  private final URI baseUri = URI.create("/");
  @SuppressWarnings("unused") // Is injected
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @Mock
  private Subject subject;

  @Mock
  private NamespaceStrategyValidator namespaceStrategyValidator;

  @InjectMocks
  private ConfigDtoToScmConfigurationMapperImpl dtoToConfigMapper;
  @InjectMocks
  private ScmConfigurationToConfigDtoMapperImpl configToDtoMapper;

  @BeforeEach
  void prepareEnvironment() {
    ConfigResource configResource = new ConfigResource(dtoToConfigMapper, configToDtoMapper, createConfiguration(), namespaceStrategyValidator);
    configResource.setStore(config -> {
    });

    dispatcher.addSingletonResource(configResource);

    ThreadContext.bind(subject);
  }

  @AfterEach
  void tearDown() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldGetGlobalConfig() throws URISyntaxException, UnsupportedEncodingException {
    doNothing().when(subject).checkPermission("configuration:read:global");

    MockHttpRequest request = MockHttpRequest.get("/" + ConfigResource.CONFIG_PATH_V2);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getContentAsString()).contains("\"proxyPassword\":\"heartOfGold\"");
    assertThat(response.getContentAsString()).contains("\"self\":{\"href\":\"/v2/config");
    assertThat(response.getContentAsString()).doesNotContain("\"update\":{\"href\":\"/v2/config");
  }

  @Test
  void shouldNotGetConfigWhenNotAuthorized() throws URISyntaxException {
    doThrow(AuthorizationException.class).when(subject).checkPermission("configuration:read:global");

    MockHttpRequest request = MockHttpRequest.get("/" + ConfigResource.CONFIG_PATH_V2);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
  }

  @Test
  void shouldUpdateConfig() throws URISyntaxException, IOException {
    doNothing().when(subject).checkPermission("configuration:write:global");

    MockHttpRequest request = put("sonia/scm/api/v2/config-test-update.json");

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
    assertThat(response.getContentAsString()).doesNotContain("\"update\":{\"href\":\"/v2/config");
  }

  @Test
  void shouldNotUpdateConfigWhenNotAuthorized() throws URISyntaxException, IOException {
    doThrow(AuthorizationException.class).when(subject).checkPermission("configuration:write:global");

    MockHttpRequest request = put("sonia/scm/api/v2/config-test-update.json");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
  }

  @Test
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
    assertThat(response.getContentAsString()).doesNotContain("\"update\":{\"href\":\"/v2/config");
  }

  @Test
  void shouldNotUpdateConfigPartiallyIfNotAuthorized() throws URISyntaxException {
    doThrow(AuthorizationException.class).when(subject).checkPermission("configuration:write:global");

    MockHttpRequest request = patch("{ \"proxyPort\":\"1337\", \"proxyPassword\":\"hitchhiker\" }");

    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
  }

  private MockHttpRequest put(String resourceName) throws IOException, URISyntaxException {
    URL url = Resources.getResource(resourceName);
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

  private static ScmConfiguration createConfiguration() {
    ScmConfiguration scmConfiguration = new ScmConfiguration();
    scmConfiguration.setProxyPassword("heartOfGold");

    return scmConfiguration;
  }
}
