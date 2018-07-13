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
import org.mockito.InjectMocks;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.web.VndMediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

@SubjectAware(
  username = "trillian",
  password = "secret",
  configuration = "classpath:sonia/scm/repository/shiro.ini"
)
public class GlobalConfigResourceTest {

  @Rule
  public ShiroRule shiro = new ShiroRule();

  private Dispatcher dispatcher = MockDispatcherFactory.createDispatcher();

  private final URI baseUri = URI.create("/");
  @SuppressWarnings("unused") // Is injected
  private ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @InjectMocks
  private GlobalConfigDtoToScmConfigurationMapperImpl dtoToConfigMapper;
  @InjectMocks
  private ScmConfigurationToGlobalConfigDtoMapperImpl configToDtoMapper;

  @Before
  public void prepareEnvironment() {
    initMocks(this);

    GlobalConfigResource globalConfigResource = new GlobalConfigResource(dtoToConfigMapper,
      configToDtoMapper, createConfiguration());

    dispatcher.getRegistry().addSingletonResource(globalConfigResource);
  }

  @Test
  public void shouldGetGlobalConfig() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/" + GlobalConfigResource.GLOBAL_CONFIG_PATH_V2);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("\"proxyPassword\":\"heartOfGold\""));
    assertTrue(response.getContentAsString().contains("\"self\":{\"href\":\"/v2/config/global"));
    assertTrue("link not found", response.getContentAsString().contains("\"update\":{\"href\":\"/v2/config/global"));
  }

  @SubjectAware(
    username = "dent"
  )
  @Test
  public void shouldGetForbiddenGlobalConfig() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/" + GlobalConfigResource.GLOBAL_CONFIG_PATH_V2);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);
    assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
  }

  @Test
  public void shouldUpdateGlobalConfig() throws URISyntaxException, IOException {
    URL url = Resources.getResource("sonia/scm/api/v2/globalConfig-test-update.json");
    byte[] configJson = Resources.toByteArray(url);
    MockHttpRequest request = MockHttpRequest.put("/" + GlobalConfigResource.GLOBAL_CONFIG_PATH_V2)
      .contentType(VndMediaType.GLOBAL_CONFIG)
      .content(configJson);

    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());

    request = MockHttpRequest.get("/" + GlobalConfigResource.GLOBAL_CONFIG_PATH_V2);
    response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("\"proxyPassword\":\"newPassword\""));
    assertTrue(response.getContentAsString().contains("\"self\":{\"href\":\"/v2/config/global"));
    assertTrue("link not found", response.getContentAsString().contains("\"update\":{\"href\":\"/v2/config/global"));

  }

  @SubjectAware(
    username = "dent"
  )
  @Test
  public void shouldUpdateForbiddenGlobalConfig() throws URISyntaxException, IOException {
    URL url = Resources.getResource("sonia/scm/api/v2/globalConfig-test-update.json");
    byte[] configJson = Resources.toByteArray(url);
    MockHttpRequest request = MockHttpRequest.put("/" + GlobalConfigResource.GLOBAL_CONFIG_PATH_V2)
      .contentType(VndMediaType.GLOBAL_CONFIG)
      .content(configJson);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);
    assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
  }

  public static ScmConfiguration createConfiguration() {
    ScmConfiguration scmConfiguration = new ScmConfiguration();
    scmConfiguration.setProxyPassword("heartOfGold");
    scmConfiguration.setProxyPort(1234);
    scmConfiguration.setProxyServer("proxyserver");
    scmConfiguration.setProxyUser("trillian");
    scmConfiguration.setEnableProxy(true);
    scmConfiguration.setRealmDescription("description");
    scmConfiguration.setEnableRepositoryArchive(true);
    scmConfiguration.setDisableGroupingGrid(true);
    scmConfiguration.setDateFormat("dd");
    scmConfiguration.setAnonymousAccessEnabled(true);
    scmConfiguration.setAdminGroups(new HashSet<>(Arrays.asList("group")));
    scmConfiguration.setAdminUsers(new HashSet<>(Arrays.asList("user1")));
    scmConfiguration.setBaseUrl("baseurl");
    scmConfiguration.setForceBaseUrl(true);
    scmConfiguration.setLoginAttemptLimit(1);
    scmConfiguration.setProxyExcludes(new HashSet<>(Arrays.asList("arthur", "dent")));
    scmConfiguration.setSkipFailedAuthenticators(true);
    scmConfiguration.setPluginUrl("pluginurl");
    scmConfiguration.setLoginAttemptLimitTimeout(2);
    scmConfiguration.setEnabledXsrfProtection(true);
    return scmConfiguration;
  }
}
