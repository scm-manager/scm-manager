/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.api.v2.resources;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.util.ThreadState;
import org.assertj.core.util.Lists;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.Tag;
import sonia.scm.repository.Tags;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.TagCommandBuilder;
import sonia.scm.repository.api.TagsCommandBuilder;
import sonia.scm.web.RestDispatcher;
import sonia.scm.web.VndMediaType;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(MockitoJUnitRunner.Silent.class)
public class TagRootResourceTest extends RepositoryTestBase {

  public static final String TAG_PATH = "space/repo/tags/";
  public static final String TAG_URL = "/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + TAG_PATH;

  private RestDispatcher dispatcher = new RestDispatcher();

  private final URI baseUri = URI.create("/");
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @Mock
  private RepositoryServiceFactory serviceFactory;

  @Mock
  private RepositoryService repositoryService;

  @Mock
  private TagsCommandBuilder tagsCommandBuilder;
  @Mock
  private TagCommandBuilder tagCommandBuilder;
  @Mock
  private TagCommandBuilder.TagCreateCommandBuilder tagCreateCommandBuilder;
  @Mock
  private TagCommandBuilder.TagDeleteCommandBuilder tagDeleteCommandBuilder;
  private TagCollectionToDtoMapper tagCollectionToDtoMapper;

  @InjectMocks
  private TagToTagDtoMapperImpl tagToTagDtoMapper;

  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);


  @Before
  public void prepareEnvironment() {
    tagCollectionToDtoMapper = new TagCollectionToDtoMapper(resourceLinks, tagToTagDtoMapper);
    tagRootResource = new TagRootResource(serviceFactory, tagCollectionToDtoMapper, tagToTagDtoMapper, resourceLinks);
    dispatcher.addSingletonResource(getRepositoryRootResource());
    when(serviceFactory.create(new NamespaceAndName("space", "repo"))).thenReturn(repositoryService);
    when(serviceFactory.create(any(Repository.class))).thenReturn(repositoryService);
    when(repositoryService.getRepository()).thenReturn(new Repository("repoId", "git", "space", "repo"));
    when(repositoryService.getTagsCommand()).thenReturn(tagsCommandBuilder);
    when(repositoryService.getTagCommand()).thenReturn(tagCommandBuilder);
    subjectThreadState.bind();
    ThreadContext.bind(subject);
    when(subject.isPermitted(any(String.class))).thenReturn(true);
    when(tagCreateCommandBuilder.setName(any())).thenReturn(tagCreateCommandBuilder);
    when(tagCreateCommandBuilder.setRevision(any())).thenReturn(tagCreateCommandBuilder);
    when(tagDeleteCommandBuilder.setName(any())).thenReturn(tagDeleteCommandBuilder);
  }

  @After
  public void cleanupContext() {
    ThreadContext.unbindSubject();
  }

  @Test
  public void shouldGet404OnMissingTag() throws Exception {
    Tags tags = new Tags();
    tags.setTags(Lists.emptyList());
    when(tagsCommandBuilder.getTags()).thenReturn(tags);

    MockHttpRequest request = MockHttpRequest
      .get(TAG_URL + "not_existing_tag")
      .accept(VndMediaType.TAG);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertEquals(404, response.getStatus());
  }

  @Test
  public void shouldGetEmptyTagListOnMissingTags() throws Exception {
    Tags tags = new Tags();
    tags.setTags(Lists.emptyList());
    when(tagsCommandBuilder.getTags()).thenReturn(tags);

    MockHttpRequest request = MockHttpRequest
      .get(TAG_URL)
      .accept(VndMediaType.TAG_COLLECTION);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertEquals(200, response.getStatus());
    assertThat(response).isNotNull();
    assertThat(response.getContentAsString())
      .isNotBlank()
      .contains("_links");
  }

  @Test
  public void shouldGet500OnTagCommandError() throws Exception {
    when(tagsCommandBuilder.getTags()).thenReturn(null);

    MockHttpRequest request = MockHttpRequest
      .get(TAG_URL + "not_existing_tag")
      .accept(VndMediaType.TAG);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertEquals(500, response.getStatus());

    request = MockHttpRequest
      .get(TAG_URL)
      .accept(VndMediaType.TAG_COLLECTION);
    response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertEquals(500, response.getStatus());
  }


  @Test
  public void shouldGetTags() throws Exception {
    Tags tags = new Tags();
    String tag1 = "v1.0";
    String revision1 = "revision_1234";
    String tag2 = "v2.0";
    String revision2 = "revision_12345";
    tags.setTags(Lists.newArrayList(new Tag(tag1, revision1), new Tag(tag2, revision2)));
    when(tagsCommandBuilder.getTags()).thenReturn(tags);

    MockHttpRequest request = MockHttpRequest
      .get(TAG_URL)
      .accept(VndMediaType.TAG_COLLECTION);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertEquals(200, response.getStatus());
    log.info("the content: ", response.getContentAsString());
    assertTrue(response.getContentAsString().contains(String.format("\"name\":\"%s\"", tag1)));
    assertTrue(response.getContentAsString().contains(String.format("\"revision\":\"%s\"", revision1)));
    assertTrue(response.getContentAsString().contains(String.format("\"name\":\"%s\"", tag2)));
    assertTrue(response.getContentAsString().contains(String.format("\"revision\":\"%s\"", revision2)));
  }

  @Test
  public void shouldGetTag() throws Exception {
    Tags tags = new Tags();
    String tag1 = "v1.0";
    String revision1 = "revision_1234";
    String tag2 = "v2.0";
    String revision2 = "revision_12345";
    tags.setTags(Lists.newArrayList(new Tag(tag1, revision1), new Tag(tag2, revision2)));
    when(tagsCommandBuilder.getTags()).thenReturn(tags);

    MockHttpRequest request = MockHttpRequest
      .get(TAG_URL + tag1)
      .accept(VndMediaType.TAG);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertEquals(200, response.getStatus());
    assertTrue(response.getContentAsString().contains(String.format("\"name\":\"%s\"", tag1)));
    assertTrue(response.getContentAsString().contains(String.format("\"revision\":\"%s\"", revision1)));

    request = MockHttpRequest
      .get(TAG_URL + tag2)
      .accept(VndMediaType.TAG);
    response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertEquals(200, response.getStatus());
    log.info("the content: ", response.getContentAsString());
    assertTrue(response.getContentAsString().contains(String.format("\"name\":\"%s\"", tag2)));
    assertTrue(response.getContentAsString().contains(String.format("\"revision\":\"%s\"", revision2)));
  }

  @Test
  public void shouldCreateTag() throws URISyntaxException, IOException {
    Tags tags = new Tags();
    tags.setTags(Lists.emptyList());
    when(tagsCommandBuilder.getTags()).thenReturn(tags);
    when(tagCommandBuilder.create()).thenReturn(tagCreateCommandBuilder);
    when(tagCreateCommandBuilder.execute()).thenReturn(new Tag("newtag", "592d797cd36432e591416e8b2b98154f4f163411"));
    MockHttpRequest request = MockHttpRequest
      .post(TAG_URL)
      .content("{\"name\": \"newtag\",\"revision\":\"592d797cd36432e591416e8b2b98154f4f163411\"}".getBytes())
      .contentType(VndMediaType.TAG_REQUEST);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(201, response.getStatus());
    assertEquals(
      URI.create("/v2/repositories/space/repo/tags/newtag"),
      response.getOutputHeaders().getFirst("Location"));
  }

  @Test
  public void shouldNotCreateTagIfNotPermitted() throws IOException, URISyntaxException {
    doThrow(AuthorizationException.class).when(subject).checkPermission("repository:push:repoId");
    Tags tags = new Tags();
    tags.setTags(Lists.emptyList());
    when(tagsCommandBuilder.getTags()).thenReturn(tags);
    when(tagCommandBuilder.create()).thenReturn(tagCreateCommandBuilder);
    when(tagCreateCommandBuilder.execute()).thenReturn(new Tag("newtag", "592d797cd36432e591416e8b2b98154f4f163411"));
    MockHttpRequest request = MockHttpRequest
      .post(TAG_URL)
      .content("{\"name\": \"newtag\",\"revision\":\"592d797cd36432e591416e8b2b98154f4f163411\"}".getBytes())
      .contentType(VndMediaType.TAG_REQUEST);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(403, response.getStatus());
    verify(tagCommandBuilder, never()).create();
  }

  @Test
  public void shouldThrowExceptionIfTagAlreadyExists() throws URISyntaxException, IOException {
    Tags tags = new Tags();
    tags.setTags(Collections.singletonList(new Tag("newtag", "592d797cd36432e591416e8b2b98154f4f163411")));
    when(tagsCommandBuilder.getTags()).thenReturn(tags);
    when(tagCommandBuilder.create()).thenReturn(tagCreateCommandBuilder);
    when(tagCreateCommandBuilder.execute()).thenReturn(new Tag("newtag", "592d797cd36432e591416e8b2b98154f4f163411"));
    MockHttpRequest request = MockHttpRequest
      .post(TAG_URL)
      .content("{\"name\": \"newtag\",\"revision\":\"592d797cd36432e591416e8b2b98154f4f163411\"}".getBytes())
      .contentType(VndMediaType.TAG_REQUEST);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(409, response.getStatus());
    verify(tagCommandBuilder, never()).create();
  }

  @Test
  public void shouldDeleteTag() throws IOException, URISyntaxException {
    Tags tags = new Tags();
    tags.setTags(Collections.singletonList(new Tag("newtag", "592d797cd36432e591416e8b2b98154f4f163411")));
    when(tagsCommandBuilder.getTags()).thenReturn(tags);
    when(tagCommandBuilder.delete()).thenReturn(tagDeleteCommandBuilder);

    MockHttpRequest request = MockHttpRequest
      .delete(TAG_URL + "newtag");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(204, response.getStatus());
    verify(tagCommandBuilder).delete();
  }

  @Test
  public void shouldReturn204EvenIfTagDoesntExist() throws IOException, URISyntaxException {
    Tags tags = new Tags();
    tags.setTags(Collections.emptyList());
    when(tagsCommandBuilder.getTags()).thenReturn(tags);
    when(tagCommandBuilder.delete()).thenReturn(tagDeleteCommandBuilder);

    MockHttpRequest request = MockHttpRequest
      .delete(TAG_URL + "newtag");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(204, response.getStatus());
    verify(tagCommandBuilder, never()).delete();
  }

  @Test
  public void shouldNotDeleteTagIfNotPermitted() throws IOException, URISyntaxException {
    doThrow(AuthorizationException.class).when(subject).checkPermission("repository:push:repoId");
    Tags tags = new Tags();
    tags.setTags(Collections.singletonList(new Tag("newtag", "592d797cd36432e591416e8b2b98154f4f163411")));
    when(tagsCommandBuilder.getTags()).thenReturn(tags);
    when(tagCommandBuilder.delete()).thenReturn(tagDeleteCommandBuilder);

    MockHttpRequest request = MockHttpRequest
      .delete(TAG_URL + "newtag");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(403, response.getStatus());
    verify(tagCommandBuilder, never()).delete();
  }
}
