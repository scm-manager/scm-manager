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
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;

import java.net.URI;
import java.net.URISyntaxException;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@SubjectAware(
  username = "trillian",
  password = "secret",
  configuration = "classpath:sonia/scm/repository/shiro.ini"
)
public class RepositoryRootResourceTest {

  private final Dispatcher dispatcher = MockDispatcherFactory.createDispatcher();

  @Rule
  public ShiroRule shiro = new ShiroRule();

  @Mock
  private RepositoryManager repositoryManager;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ResourceLinks resourceLinks;
  @InjectMocks
  private RepositoryToRepositoryDtoMapperImpl repositoryToDtoMapper;

  @Before
  public void prepareEnvironment() {
    initMocks(this);
    ResourceLinksMock.initMock(resourceLinks, URI.create("/"));
    RepositoryResource repositoryResource = new RepositoryResource(repositoryToDtoMapper, repositoryManager);
    RepositoryRootResource repositoryRootResource = new RepositoryRootResource(MockProvider.of(repositoryResource));
    dispatcher.getRegistry().addSingletonResource(repositoryRootResource);
  }

  @Test
  public void shouldFailForNotExistingRepository() throws URISyntaxException {
    mockRepository("space", "repo");

    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/other");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_NOT_FOUND, response.getStatus());
  }

  @Test
  public void shouldFindExistingRepository() throws URISyntaxException {
    mockRepository("space", "repo");

    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("\"name\":\"repo\""));
  }

  private Repository mockRepository(String namespace, String name) {
    Repository repository = new Repository();
    repository.setNamespace(namespace);
    repository.setName(name);
    repository.setId("id");
    when(repositoryManager.getByNamespace(namespace, name)).thenReturn(repository);
    return repository;
  }
}
