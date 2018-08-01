package sonia.scm.api.v2.resources;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import sonia.scm.repository.GitConfig;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.store.ConfigurationStoreFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@SubjectAware(
  username = "trillian",
  password = "secret",
  configuration = "classpath:sonia/scm/repository/shiro.ini"
)
@RunWith(MockitoJUnitRunner.class)
public class GitConfigResourceTest {

  @Rule
  public ShiroRule shiro = new ShiroRule();

  private Dispatcher dispatcher = MockDispatcherFactory.createDispatcher();

  private final URI baseUri = URI.create("/");

  @InjectMocks
  private GitConfigDtoToGitConfigMapperImpl dtoToConfigMapper;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private UriInfoStore uriInfoStore;

  @InjectMocks
  private GitConfigToGitConfigDtoMapperImpl configToDtoMapper;

  @Mock
  ConfigurationStoreFactory storeFactory;
  
  @InjectMocks
  private GitRepositoryHandler repositoryHandler;

  @Before
  public void prepareEnvironment() {
    GitConfig gitConfig = createConfiguration();
    repositoryHandler.setConfig(gitConfig);
    GitConfigResource gitConfigResource = new GitConfigResource(dtoToConfigMapper, configToDtoMapper, repositoryHandler);
    dispatcher.getRegistry().addSingletonResource(gitConfigResource);
    when(uriInfoStore.get().getBaseUri()).thenReturn(baseUri);
  }

  @Test
  public void shouldGetGitConfig() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/" + GitConfigResource.GIT_CONFIG_PATH_V2);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("\"disabled\":false"));
    //assertTrue(response.getContentAsString().contains("\"repository-directory\":\"repository/directory\""));
    //assertTrue(response.getContentAsString().contains("\"gc-expression\":\"valid Git GC Cron Expression\""));
    assertTrue(response.getContentAsString().contains("\"self\":{\"href\":\"/v2/config/git"));
    assertTrue(response.getContentAsString().contains("\"update\":{\"href\":\"/v2/config/git"));
  }

  // TODO negative tests

  private GitConfig createConfiguration() {
    GitConfig config = new GitConfig();
    //config.setGcExpression("valid Git GC Cron Expression");
    config.setDisabled(false);
    config.setRepositoryDirectory(new File("repository/directory"));
    return config;
  }

}

