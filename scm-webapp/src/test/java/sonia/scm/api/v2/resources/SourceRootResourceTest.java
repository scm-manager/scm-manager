package sonia.scm.api.v2.resources;

import com.google.inject.util.Providers;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.NotFoundException;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.api.BrowseCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.web.RestDispatcher;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.Silent.class)
public class SourceRootResourceTest extends RepositoryTestBase {

  private RestDispatcher dispatcher = new RestDispatcher();
  private final URI baseUri = URI.create("/");
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private RepositoryService service;
  @Mock
  private BrowseCommandBuilder browseCommandBuilder;

  private BrowserResultToFileObjectDtoMapper browserResultToFileObjectDtoMapper;


  @Before
  public void prepareEnvironment() {
    browserResultToFileObjectDtoMapper = Mappers.getMapper(BrowserResultToFileObjectDtoMapper.class);
    browserResultToFileObjectDtoMapper.setResourceLinks(resourceLinks);
    when(serviceFactory.create(new NamespaceAndName("space", "repo"))).thenReturn(service);
    when(service.getBrowseCommand()).thenReturn(browseCommandBuilder);

    SourceRootResource sourceRootResource = new SourceRootResource(serviceFactory, browserResultToFileObjectDtoMapper);
    super.sourceRootResource = Providers.of(sourceRootResource);
    dispatcher.addSingletonResource(getRepositoryRootResource());
  }

  @Test
  public void shouldReturnSources() throws URISyntaxException, IOException {
    BrowserResult result = createBrowserResult();
    when(browseCommandBuilder.getBrowserResult()).thenReturn(result);
    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/sources");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);
    assertThat(response.getStatus()).isEqualTo(200);
    System.out.println(response.getContentAsString());
    assertThat(response.getContentAsString()).contains("\"revision\":\"revision\"");
    assertThat(response.getContentAsString()).contains("\"children\":");
  }

  @Test
  public void shouldReturn404IfRepoNotFound() throws URISyntaxException {
    when(serviceFactory.create(new NamespaceAndName("idont", "exist"))).thenThrow(new NotFoundException("Test", "a"));
    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "idont/exist/sources");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);
    assertThat(response.getStatus()).isEqualTo(404);
  }

  @Test
  public void shouldGetResultForSingleFile() throws URISyntaxException, IOException {
    FileObject fileObject = new FileObject();
    fileObject.setName("File Object!");
    fileObject.setPath("/");
    BrowserResult browserResult = new BrowserResult("revision", fileObject);

    when(browseCommandBuilder.getBrowserResult()).thenReturn(browserResult);
    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/sources/revision/fileabc");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(response.getContentAsString()).contains("\"revision\":\"revision\"");
  }

  @Test
  public void shouldGet404ForSingleFileIfRepoNotFound() throws URISyntaxException {
    when(serviceFactory.create(new NamespaceAndName("idont", "exist"))).thenThrow(new NotFoundException("Test", "a"));

    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "idont/exist/sources/revision/fileabc");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);
    assertThat(response.getStatus()).isEqualTo(404);
  }

  private BrowserResult createBrowserResult() {
    return new BrowserResult("revision", createFileObject());
  }

  private FileObject createFileObject() {
    FileObject parent = new FileObject();
    parent.setName("bar");
    parent.setPath("/foo/bar");
    parent.setDirectory(true);

    FileObject fileObject1 = new FileObject();
    fileObject1.setName("FO 1");
    fileObject1.setDirectory(false);
    fileObject1.setDescription("File object 1");
    fileObject1.setPath("/foo/bar/fo1");
    fileObject1.setLength(1024L);
    fileObject1.setCommitDate(0L);
    parent.addChild(fileObject1);

    FileObject fileObject2 = new FileObject();
    fileObject2.setName("FO 2");
    fileObject2.setDirectory(true);
    fileObject2.setDescription("File object 2");
    fileObject2.setPath("/foo/bar/fo2");
    fileObject2.setLength(4096L);
    fileObject2.setCommitDate(1234L);
    parent.addChild(fileObject2);

    return parent;
  }
}
