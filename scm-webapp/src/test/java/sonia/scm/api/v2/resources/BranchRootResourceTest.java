package sonia.scm.api.v2.resources;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.repository.Branch;
import sonia.scm.repository.Branches;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.api.BranchesCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class BranchRootResourceTest {

  private final Dispatcher dispatcher = MockDispatcherFactory.createDispatcher();

  private final URI baseUri = URI.create("/");
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private RepositoryService service;
  @Mock
  private BranchesCommandBuilder branchesCommandBuilder;

  @InjectMocks
  private BranchToBranchDtoMapperImpl branchToDtoMapper;

  @Before
  public void prepareEnvironment() throws Exception {
    BranchCollectionToDtoMapper branchCollectionToDtoMapper = new BranchCollectionToDtoMapper(branchToDtoMapper, resourceLinks);
    BranchRootResource branchRootResource = new BranchRootResource(serviceFactory, branchToDtoMapper, branchCollectionToDtoMapper);
    RepositoryRootResource repositoryRootResource = new RepositoryRootResource(MockProvider.of(new RepositoryResource(null, null, null, null, MockProvider.of(branchRootResource), null, null, null)), null);
    dispatcher.getRegistry().addSingletonResource(repositoryRootResource);

    when(serviceFactory.create(new NamespaceAndName("space", "repo"))).thenReturn(service);
    when(service.getBranchesCommand()).thenReturn(branchesCommandBuilder);
  }

  @Test
  public void shouldHandleMissingBranch() throws Exception {
    when(branchesCommandBuilder.getBranches()).thenReturn(new Branches());

    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/branches/master");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(404, response.getStatus());
  }

  @Test
  public void shouldFindExistingBranch() throws Exception {
    when(branchesCommandBuilder.getBranches()).thenReturn(new Branches(new Branch("master", "revision")));

    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/branches/master");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(200, response.getStatus());
    System.out.println(response.getContentAsString());
    assertTrue(response.getContentAsString().contains("\"revision\":\"revision\""));
  }
}
