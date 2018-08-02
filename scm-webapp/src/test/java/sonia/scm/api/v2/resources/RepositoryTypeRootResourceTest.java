package sonia.scm.api.v2.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryType;
import sonia.scm.web.VndMediaType;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RepositoryTypeRootResourceTest {

  private final Dispatcher dispatcher = MockDispatcherFactory.createDispatcher();

  @Mock
  private RepositoryManager repositoryManager;

  private final URI baseUri = URI.create("/");
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @InjectMocks
  private RepositoryTypeToRepositoryTypeDtoMapperImpl mapper;

  private List<RepositoryType> types = Lists.newArrayList(
    new RepositoryType("hk", "Hitchhiker", Sets.newHashSet()),
    new RepositoryType("hog", "Heart of Gold", Sets.newHashSet())
  );

  @Before
  public void prepareEnvironment() {
    when(repositoryManager.getConfiguredTypes()).thenReturn(types);

    RepositoryTypeCollectionToDtoMapper collectionMapper = new RepositoryTypeCollectionToDtoMapper(mapper, resourceLinks);
    RepositoryTypeCollectionResource collectionResource = new RepositoryTypeCollectionResource(repositoryManager, collectionMapper);
    RepositoryTypeResource resource = new RepositoryTypeResource(repositoryManager, mapper);
    RepositoryTypeRootResource rootResource = new RepositoryTypeRootResource(MockProvider.of(collectionResource), MockProvider.of(resource));
    dispatcher.getRegistry().addSingletonResource(rootResource);
  }

  @Test
  public void shouldHaveCollectionVndMediaType() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryTypeRootResource.PATH);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_OK, response.getStatus());
    String contentType = response.getOutputHeaders().getFirst("Content-Type").toString();
    assertThat(VndMediaType.REPOSITORY_TYPE_COLLECTION, equalToIgnoringCase(contentType));
  }

  @Test
  public void shouldHaveCollectionSelfLink() throws URISyntaxException {
    String uri = "/" + RepositoryTypeRootResource.PATH;
    MockHttpRequest request = MockHttpRequest.get(uri);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("\"self\":{\"href\":\"" + uri + "\"}"));
  }

  @Test
  public void shouldHaveEmbeddedRepositoryTypes() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryTypeRootResource.PATH);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("Hitchhiker"));
    assertTrue(response.getContentAsString().contains("Heart of Gold"));
  }

  @Test
  public void shouldHaveVndMediaType() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryTypeRootResource.PATH + "hk");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_OK, response.getStatus());
    String contentType = response.getOutputHeaders().getFirst("Content-Type").toString();
    assertThat(VndMediaType.REPOSITORY_TYPE, equalToIgnoringCase(contentType));
  }

  @Test
  public void shouldContainAttributes() throws URISyntaxException {
    String uri = "/" + RepositoryTypeRootResource.PATH + "hk";
    MockHttpRequest request = MockHttpRequest.get(uri);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("hk"));
    assertTrue(response.getContentAsString().contains("Hitchhiker"));
  }

  @Test
  public void shouldHaveSelfLink() throws URISyntaxException {
    String uri = "/" + RepositoryTypeRootResource.PATH + "hk";
    MockHttpRequest request = MockHttpRequest.get(uri);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("\"self\":{\"href\":\"" + uri + "\"}"));
  }

  @Test
  public void shouldReturn404OnUnknownTypes() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryTypeRootResource.PATH + "git");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_NOT_FOUND, response.getStatus());
  }

}
