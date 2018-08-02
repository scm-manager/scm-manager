package sonia.scm.api.v2.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import sonia.scm.repository.HgConfig;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.web.HgVndMediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

@SubjectAware(
  configuration = "classpath:sonia/scm/configuration/shiro.ini",
  password = "secret"
)
@RunWith(MockitoJUnitRunner.class)
public class HgConfigResourceTest {

  @Rule
  public ShiroRule shiro = new ShiroRule();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private Dispatcher dispatcher = MockDispatcherFactory.createDispatcher();

  private final URI baseUri = URI.create("/");

  @InjectMocks
  private HgConfigDtoToHgConfigMapperImpl dtoToConfigMapper;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private UriInfoStore uriInfoStore;

  @InjectMocks
  private HgConfigToHgConfigDtoMapperImpl configToDtoMapper;

  @Mock
  private HgRepositoryHandler repositoryHandler;

  @Before
  public void prepareEnvironment() {
    HgConfig gitConfig = createConfiguration();
    when(repositoryHandler.getConfig()).thenReturn(gitConfig);
    HgConfigResource gitConfigResource = new HgConfigResource(dtoToConfigMapper, configToDtoMapper, repositoryHandler);
    dispatcher.getRegistry().addSingletonResource(gitConfigResource);
    when(uriInfoStore.get().getBaseUri()).thenReturn(baseUri);
  }

  @Test
  @SubjectAware(username = "readWrite")
  public void shouldGetHgConfig() throws URISyntaxException, IOException {
    MockHttpResponse response = get();

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());

    String responseString = response.getContentAsString();
    ObjectNode responseJson = new ObjectMapper().readValue(responseString, ObjectNode.class);

    assertTrue(responseString.contains("\"disabled\":false"));
    assertTrue(responseJson.get("repositoryDirectory").asText().endsWith("repository/directory"));
    assertTrue(responseString.contains("\"self\":{\"href\":\"/v2/config/hg"));
    assertTrue(responseString.contains("\"update\":{\"href\":\"/v2/config/hg"));
  }

  @Test
  @SubjectAware(username = "readWrite")
  public void shouldGetHgConfigEvenWhenItsEmpty() throws URISyntaxException, IOException {
    when(repositoryHandler.getConfig()).thenReturn(null);

    MockHttpResponse response = get();
    String responseString = response.getContentAsString();

    assertTrue(responseString.contains("\"disabled\":false"));
  }

  @Test
  @SubjectAware(username = "readOnly")
  public void shouldGetHgConfigWithoutUpdateLink() throws URISyntaxException {
    MockHttpResponse response = get();

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());

    assertFalse(response.getContentAsString().contains("\"update\":{\"href\":\"/v2/config/hg"));
  }

  @Test
  @SubjectAware(username = "writeOnly")
  public void shouldGetConfigOnlyWhenAuthorized() throws URISyntaxException {
    thrown.expectMessage("Subject does not have permission [configuration:read:hg]");

    get();
  }

  @Test
  @SubjectAware(username = "writeOnly")
  public void shouldUpdateConfig() throws URISyntaxException {
    MockHttpResponse response = put();
    assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
  }

  @Test
  @SubjectAware(username = "readOnly")
  public void shouldUpdateConfigOnlyWhenAuthorized() throws URISyntaxException {
    thrown.expectMessage("Subject does not have permission [configuration:write:hg]");

    put();
  }

  private MockHttpResponse get() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/" + HgConfigResource.HG_CONFIG_PATH_V2);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    return response;
  }

  private MockHttpResponse put() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.put("/" + HgConfigResource.HG_CONFIG_PATH_V2)
                                             .contentType(HgVndMediaType.HG_CONFIG)
                                             .content("{\"disabled\":true}".getBytes());

    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    return response;
  }

  private HgConfig createConfiguration() {
    HgConfig config = new HgConfig();
    config.setDisabled(false);
    config.setRepositoryDirectory(new File("repository/directory"));
    return config;
  }

}

