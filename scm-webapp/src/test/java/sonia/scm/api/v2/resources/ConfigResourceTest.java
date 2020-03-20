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

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.common.io.Resources;
import org.apache.shiro.util.ThreadContext;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

@SubjectAware(
  configuration = "classpath:sonia/scm/configuration/shiro.ini",
  password = "secret"
)
public class ConfigResourceTest {

  @Rule
  public ShiroRule shiro = new ShiroRule();

  private RestDispatcher dispatcher = new RestDispatcher();

  private final URI baseUri = URI.create("/");
  @SuppressWarnings("unused") // Is injected
  private ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @Mock
  private NamespaceStrategyValidator namespaceStrategyValidator;

  @InjectMocks
  private ConfigDtoToScmConfigurationMapperImpl dtoToConfigMapper;
  @InjectMocks
  private ScmConfigurationToConfigDtoMapperImpl configToDtoMapper;

  public ConfigResourceTest() {
      // cleanup state that might have been left by other tests
      ThreadContext.unbindSecurityManager();
      ThreadContext.unbindSubject();
      ThreadContext.remove();
  }

  @Before
  public void prepareEnvironment() {
    initMocks(this);

    ConfigResource configResource = new ConfigResource(dtoToConfigMapper, configToDtoMapper, createConfiguration(), namespaceStrategyValidator);

    dispatcher.addSingletonResource(configResource);
  }

  @Test
  @SubjectAware(username = "readOnly")
  public void shouldGetGlobalConfig() throws URISyntaxException, UnsupportedEncodingException {
    MockHttpRequest request = MockHttpRequest.get("/" + ConfigResource.CONFIG_PATH_V2);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("\"proxyPassword\":\"heartOfGold\""));
    assertTrue(response.getContentAsString().contains("\"self\":{\"href\":\"/v2/config"));
    assertFalse("Update link present", response.getContentAsString().contains("\"update\":{\"href\":\"/v2/config"));
  }

  @Test
  @SubjectAware(username = "writeOnly")
  public void shouldNotGetConfigWhenNotAuthorized() throws URISyntaxException, UnsupportedEncodingException {
    MockHttpRequest request = MockHttpRequest.get("/" + ConfigResource.CONFIG_PATH_V2);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals("Subject does not have permission [configuration:read:global]", response.getContentAsString());
    assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
  }

  @Test
  @SubjectAware(username = "readWrite")
  public void shouldUpdateConfig() throws URISyntaxException, IOException {
    MockHttpRequest request = post("sonia/scm/api/v2/config-test-update.json");

    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());

    request = MockHttpRequest.get("/" + ConfigResource.CONFIG_PATH_V2);
    response = new MockHttpResponse();
    dispatcher.invoke(request, response);    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("\"proxyPassword\":\"newPassword\""));
    assertTrue(response.getContentAsString().contains("\"self\":{\"href\":\"/v2/config"));
    assertTrue("link not found", response.getContentAsString().contains("\"update\":{\"href\":\"/v2/config"));
  }

  @Test
  @SubjectAware(username = "readOnly")
  public void shouldNotUpdateConfigWhenNotAuthorized() throws URISyntaxException, IOException {
    MockHttpRequest request = post("sonia/scm/api/v2/config-test-update.json");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals("Subject does not have permission [configuration:write:global]", response.getContentAsString());
    assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
  }



  @Test
  @SubjectAware(username = "readWrite")
  public void shouldValidateNamespaceStrategy() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.put("/" + ConfigResource.CONFIG_PATH_V2)
      .contentType(VndMediaType.CONFIG)
      .content("{ \"namespaceStrategy\": \"AwesomeStrategy\" }".getBytes(StandardCharsets.UTF_8));

    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
    verify(namespaceStrategyValidator).check("AwesomeStrategy");
  }

  private MockHttpRequest post(String resourceName) throws IOException, URISyntaxException {
    URL url = Resources.getResource(resourceName);
    byte[] configJson = Resources.toByteArray(url);
    return MockHttpRequest.put("/" + ConfigResource.CONFIG_PATH_V2)
      .contentType(VndMediaType.CONFIG)
      .content(configJson);
  }

  private static ScmConfiguration createConfiguration() {
    ScmConfiguration scmConfiguration = new ScmConfiguration();
    scmConfiguration.setProxyPassword("heartOfGold");

    return scmConfiguration;
  }
}
