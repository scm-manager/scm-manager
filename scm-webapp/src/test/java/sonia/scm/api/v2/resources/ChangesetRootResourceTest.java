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
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.util.ThreadState;
import org.assertj.core.util.Lists;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.Feature;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Person;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.LogCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.web.JsonMockHttpResponse;
import sonia.scm.web.RestDispatcher;
import sonia.scm.web.VndMediaType;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mock.Strictness.LENIENT;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class ChangesetRootResourceTest extends RepositoryTestBase {

  static final String CHANGESET_PATH = "space/repo/changesets/";
  static final String CHANGESET_URL = "/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + CHANGESET_PATH;

  private final RestDispatcher dispatcher = new RestDispatcher();

  private final URI baseUri = URI.create("/");
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @Mock(strictness = LENIENT)
  private RepositoryServiceFactory serviceFactory;

  @Mock(strictness = LENIENT)
  private RepositoryService repositoryService;

  @Mock(strictness = LENIENT)
  private LogCommandBuilder logCommandBuilder;

  @Mock
  private TagCollectionToDtoMapper tagCollectionToDtoMapper;

  @InjectMocks
  private ChangesetCollectionToDtoMapper changesetCollectionToDtoMapper;


  @InjectMocks
  private DefaultChangesetToChangesetDtoMapperImpl changesetToChangesetDtoMapper;

  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);

  @BeforeEach
  void prepareEnvironment() {
    changesetCollectionToDtoMapper = new ChangesetCollectionToDtoMapper(changesetToChangesetDtoMapper, resourceLinks);
    changesetRootResource = new ChangesetRootResource(serviceFactory, changesetCollectionToDtoMapper, changesetToChangesetDtoMapper);
    dispatcher.addSingletonResource(getRepositoryRootResource());
    when(serviceFactory.create(new NamespaceAndName("space", "repo"))).thenReturn(repositoryService);
    when(serviceFactory.create(any(Repository.class))).thenReturn(repositoryService);
    when(repositoryService.getRepository()).thenReturn(new Repository("repoId", "git", "space", "repo"));
    when(repositoryService.getLogCommand()).thenReturn(logCommandBuilder);
    subjectThreadState.bind();
    ThreadContext.bind(subject);
    when(subject.isPermitted(any(String.class))).thenReturn(true);
  }

  @AfterEach
  void cleanupContext() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldGetChangeSets() throws Exception {
    String id = "revision_123";
    Instant creationDate = Instant.now();
    String authorName = "name";
    String authorEmail = "em@i.l";
    String commit = "my branch commit";
    ChangesetPagingResult changesetPagingResult = mock(ChangesetPagingResult.class);
    List<Changeset> changesetList = Lists.newArrayList(new Changeset(id, Date.from(creationDate).getTime(), new Person(authorName, authorEmail), commit));
    when(changesetPagingResult.getChangesets()).thenReturn(changesetList);
    when(changesetPagingResult.getTotal()).thenReturn(1);
    when(logCommandBuilder.setPagingStart(0)).thenReturn(logCommandBuilder);
    when(logCommandBuilder.setPagingLimit(10)).thenReturn(logCommandBuilder);
    when(logCommandBuilder.setBranch(anyString())).thenReturn(logCommandBuilder);
    when(logCommandBuilder.getChangesets()).thenReturn(changesetPagingResult);
    MockHttpRequest request = MockHttpRequest
      .get(CHANGESET_URL)
      .accept(VndMediaType.CHANGESET_COLLECTION);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertEquals(200, response.getStatus());
    log.info("Response :{}", response.getContentAsString());
    assertTrue(response.getContentAsString().contains(String.format("\"id\":\"%s\"", id)));
    assertTrue(response.getContentAsString().contains(String.format("\"name\":\"%s\"", authorName)));
    assertTrue(response.getContentAsString().contains(String.format("\"mail\":\"%s\"", authorEmail)));
    assertTrue(response.getContentAsString().contains(String.format("\"description\":\"%s\"", commit)));
  }

  @Test
  void shouldGetSinglePageOfChangeSets() throws Exception {
    String id = "revision_123";
    Instant creationDate = Instant.now();
    String authorName = "name";
    String authorEmail = "em@i.l";
    String commit = "my branch commit";
    ChangesetPagingResult changesetPagingResult = mock(ChangesetPagingResult.class);
    List<Changeset> changesetList = Lists.newArrayList(new Changeset(id, Date.from(creationDate).getTime(), new Person(authorName, authorEmail), commit));
    when(changesetPagingResult.getChangesets()).thenReturn(changesetList);
    when(changesetPagingResult.getTotal()).thenReturn(1);
    when(logCommandBuilder.setPagingStart(20)).thenReturn(logCommandBuilder);
    when(logCommandBuilder.setPagingLimit(10)).thenReturn(logCommandBuilder);
    when(logCommandBuilder.setBranch(anyString())).thenReturn(logCommandBuilder);
    when(logCommandBuilder.getChangesets()).thenReturn(changesetPagingResult);
    MockHttpRequest request = MockHttpRequest
      .get(CHANGESET_URL + "?page=2")
      .accept(VndMediaType.CHANGESET_COLLECTION);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertEquals(200, response.getStatus());
    log.info("Response :{}", response.getContentAsString());
    assertTrue(response.getContentAsString().contains(String.format("\"id\":\"%s\"", id)));
    assertTrue(response.getContentAsString().contains(String.format("\"name\":\"%s\"", authorName)));
    assertTrue(response.getContentAsString().contains(String.format("\"mail\":\"%s\"", authorEmail)));
    assertTrue(response.getContentAsString().contains(String.format("\"description\":\"%s\"", commit)));
  }

  @Nested
  class ForExistingChangeset {

    private final String id = "revision_123";
    private final Instant creationDate = Instant.now();
    private final String authorName = "name";
    private final String authorEmail = "em@i.l";
    private final String commit = "my branch commit";

    private final JsonMockHttpResponse response = new JsonMockHttpResponse();

    @BeforeEach
    void prepareExistingChangeset() throws URISyntaxException, IOException {
      when(logCommandBuilder.getChangeset(id)).thenReturn(
        new Changeset(id, Date.from(creationDate).getTime(), new Person(authorName, authorEmail), commit)
      );
    }

    private void executeRequest() throws URISyntaxException {
      MockHttpRequest request = MockHttpRequest
        .get(CHANGESET_URL + id)
        .accept(VndMediaType.CHANGESET);
      dispatcher.invoke(request, response);
    }

    @Test
    void shouldGetChangeSet() throws URISyntaxException {
      executeRequest();

      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(response.getContentAsJson().get("id").asText()).isEqualTo(id);
      assertThat(response.getContentAsJson().get("author").get("name").asText()).isEqualTo(authorName);
      assertThat(response.getContentAsJson().get("author").get("mail").asText()).isEqualTo(authorEmail);
      assertThat(response.getContentAsJson().get("description").asText()).isEqualTo(commit);
    }

    @Test
    void shouldContainLinkForTagCreation() throws URISyntaxException {
      when(subject.isPermitted("repository:push:repoId")).thenReturn(true);
      when(repositoryService.isSupported(Command.TAG)).thenReturn(true);

      executeRequest();

      assertThat(response.getContentAsJson().get("_links").get("tag").get("href").asText())
        .isEqualTo("/v2/repositories/space/repo/tags/");
    }

    @Test
    void shouldContainLinkForTagsForRevision() throws URISyntaxException {
      when(repositoryService.isSupported(Command.TAGS)).thenReturn(true);
      when(repositoryService.isSupported(Feature.TAGS_FOR_REVISION)).thenReturn(true);

      executeRequest();

      assertThat(response.getContentAsJson().get("_links").get("containedInTags").get("href").asText())
        .isEqualTo("/v2/repositories/space/repo/tags/contains/revision_123");
    }
  }

  @Test
  void shouldReturnNotFoundForNonExistingChangeset() throws Exception {
    MockHttpRequest request = MockHttpRequest
      .get(CHANGESET_URL + "abcd")
      .accept(VndMediaType.CHANGESET);

    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);

    assertEquals(404, response.getStatus());
  }

}
