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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import sonia.scm.PageResult;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;

import java.net.URI;
import java.net.URISyntaxException;

import static java.util.Collections.singletonList;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
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

  private final URI baseUri = URI.create("/");
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @InjectMocks
  private RepositoryToRepositoryDtoMapperImpl repositoryToDtoMapper;

  @Before
  public void prepareEnvironment() {
    initMocks(this);
    RepositoryResource repositoryResource = new RepositoryResource(repositoryToDtoMapper, repositoryManager, null, null, null, null, null);
    RepositoryCollectionToDtoMapper repositoryCollectionToDtoMapper = new RepositoryCollectionToDtoMapper(repositoryToDtoMapper, resourceLinks);
    RepositoryCollectionResource repositoryCollectionResource = new RepositoryCollectionResource(repositoryManager, repositoryCollectionToDtoMapper);
    RepositoryRootResource repositoryRootResource = new RepositoryRootResource(MockProvider.of(repositoryResource), MockProvider.of(repositoryCollectionResource));
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

  @Test
  public void shouldGetAll() throws URISyntaxException {
    PageResult<Repository> singletonPageResult = createSingletonPageResult(mockRepository("space", "repo"));
    when(repositoryManager.getPage(any(), eq(0), eq(10))).thenReturn(singletonPageResult);

    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("\"name\":\"repo\""));
  }

  private PageResult<Repository> createSingletonPageResult(Repository repository) {
    return new PageResult<>(singletonList(repository), 0);
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
