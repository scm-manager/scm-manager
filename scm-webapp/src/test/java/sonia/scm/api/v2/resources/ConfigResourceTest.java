package sonia.scm.api.v2.resources;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.common.io.Resources;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.web.VndMediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

@SubjectAware(
  configuration = "classpath:sonia/scm/configuration/shiro.ini",
  password = "secret"
)
public class ConfigResourceTest {

  @Rule
  public ShiroRule shiro = new ShiroRule();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private Dispatcher dispatcher = MockDispatcherFactory.createDispatcher();

  private final URI baseUri = URI.create("/");
  @SuppressWarnings("unused") // Is injected
  private ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @InjectMocks
  private ConfigDtoToScmConfigurationMapperImpl dtoToConfigMapper;
  @InjectMocks
  private ScmConfigurationToConfigDtoMapperImpl configToDtoMapper;

  @Before
  public void prepareEnvironment() {
    initMocks(this);

    ConfigResource configResource = new ConfigResource(dtoToConfigMapper, configToDtoMapper, createConfiguration());

    dispatcher.getRegistry().addSingletonResource(configResource);
  }

  @Test
  @SubjectAware(username = "readOnly")
  public void shouldGetGlobalConfig() throws URISyntaxException {
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
  public void shouldGetConfigOnlyWhenAuthorized() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/" + ConfigResource.CONFIG_PATH_V2);
    MockHttpResponse response = new MockHttpResponse();

    thrown.expectMessage("Subject does not have permission [configuration:read:global]");

    dispatcher.invoke(request, response);
  }

  @Test
  @SubjectAware(username = "readWrite")
  public void shouldUpdateConfig() throws URISyntaxException, IOException {
    URL url = Resources.getResource("sonia/scm/api/v2/config-test-update.json");
    byte[] configJson = Resources.toByteArray(url);
    MockHttpRequest request = MockHttpRequest.put("/" + ConfigResource.CONFIG_PATH_V2)
      .contentType(VndMediaType.CONFIG)
      .content(configJson);

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
  public void shouldUpdateConfigOnlyWhenAuthorized() throws URISyntaxException, IOException {
    URL url = Resources.getResource("sonia/scm/api/v2/config-test-update.json");
    byte[] configJson = Resources.toByteArray(url);
    MockHttpRequest request = MockHttpRequest.put("/" + ConfigResource.CONFIG_PATH_V2)
      .contentType(VndMediaType.CONFIG)
      .content(configJson);
    MockHttpResponse response = new MockHttpResponse();

    thrown.expectMessage("Subject does not have permission [configuration:write:global]");

    dispatcher.invoke(request, response);
  }

  private static ScmConfiguration createConfiguration() {
    ScmConfiguration scmConfiguration = new ScmConfiguration();
    scmConfiguration.setProxyPassword("heartOfGold");

    return scmConfiguration;
  }
}
