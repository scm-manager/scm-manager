/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.api.v2.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.util.Providers;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryType;
import sonia.scm.web.RestDispatcher;
import sonia.scm.web.VndMediaType;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class RepositoryTypeRootResourceTest {

  private final RestDispatcher dispatcher = new RestDispatcher();

  @Mock
  private RepositoryManager repositoryManager;
  @Mock
  private Subject subject;

  private final URI baseUri = URI.create("/");
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @InjectMocks
  private RepositoryTypeToRepositoryTypeDtoMapperImpl mapper;

  private final List<RepositoryType> types = Lists.newArrayList(
    new RepositoryType("hk", "Hitchhiker", Sets.newHashSet()),
    new RepositoryType("hog", "Heart of Gold", Sets.newHashSet())
  );

  @Before
  public void prepareEnvironment() {
    ThreadContext.bind(subject);

    when(repositoryManager.getConfiguredTypes()).thenReturn(types);

    RepositoryTypeCollectionToDtoMapper collectionMapper = new RepositoryTypeCollectionToDtoMapper(mapper, resourceLinks);
    RepositoryTypeCollectionResource collectionResource = new RepositoryTypeCollectionResource(repositoryManager, collectionMapper);
    RepositoryTypeResource resource = new RepositoryTypeResource(repositoryManager, mapper);
    RepositoryTypeRootResource rootResource = new RepositoryTypeRootResource(Providers.of(collectionResource), Providers.of(resource));
    dispatcher.addSingletonResource(rootResource);
  }

  @After
  public void tearDown() {
    ThreadContext.unbindSubject();
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
  public void shouldHaveCollectionSelfLink() throws Exception {
    String uri = "/" + RepositoryTypeRootResource.PATH;
    MockHttpRequest request = MockHttpRequest.get(uri);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("\"self\":{\"href\":\"" + uri + "\"}"));
  }

  @Test
  public void shouldHaveEmbeddedRepositoryTypes() throws Exception {
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
  public void shouldContainAttributes() throws Exception {
    String uri = "/" + RepositoryTypeRootResource.PATH + "hk";
    MockHttpRequest request = MockHttpRequest.get(uri);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("hk"));
    assertTrue(response.getContentAsString().contains("Hitchhiker"));
  }

  @Test
  public void shouldHaveSelfLink() throws Exception {
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
