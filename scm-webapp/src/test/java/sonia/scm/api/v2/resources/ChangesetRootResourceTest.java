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

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.UnauthorizedException;
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
import org.mockito.Answers;
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
import sonia.scm.repository.api.RevertCommandBuilder;
import sonia.scm.repository.api.RevertCommandResult;
import sonia.scm.web.JsonMockHttpRequest;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mock.Strictness.LENIENT;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

  @Mock(strictness = LENIENT, answer = Answers.RETURNS_DEEP_STUBS)
  private ChangesetToParentDtoMapper changesetToParentDtoMapper;

  @InjectMocks
  private ChangesetCollectionToDtoMapper changesetCollectionToDtoMapper;

  @SuppressWarnings("unused")
  @Mock
  private TagCollectionToDtoMapper tagCollectionToDtoMapper;

  @InjectMocks
  private DefaultChangesetToChangesetDtoMapperImpl changesetToChangesetDtoMapper;

  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);

  @BeforeEach
  void prepareEnvironment() {
    changesetCollectionToDtoMapper = new ChangesetCollectionToDtoMapper(changesetToChangesetDtoMapper, resourceLinks);
    changesetRootResource = new ChangesetRootResource(serviceFactory, changesetCollectionToDtoMapper, changesetToChangesetDtoMapper, resourceLinks);
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
    assertThat(response.getStatus()).isEqualTo(200);
    log.info("Response :{}", response.getContentAsString());
    assertThat(response.getContentAsString()).contains(String.format("\"id\":\"%s\"", id));
    assertThat(response.getContentAsString()).contains(String.format("\"name\":\"%s\"", authorName));
    assertThat(response.getContentAsString()).contains(String.format("\"mail\":\"%s\"", authorEmail));
    assertThat(response.getContentAsString()).contains(String.format("\"description\":\"%s\"", commit));
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
    assertThat(response.getStatus()).isEqualTo(200);
    log.info("Response :{}", response.getContentAsString());
    assertThat(response.getContentAsString()).contains(String.format("\"id\":\"%s\"", id));
    assertThat(response.getContentAsString()).contains(String.format("\"name\":\"%s\"", authorName));
    assertThat(response.getContentAsString()).contains(String.format("\"mail\":\"%s\"", authorEmail));
    assertThat(response.getContentAsString()).contains(String.format("\"description\":\"%s\"", commit));
  }

  @Nested
  class ForExistingChangeset {

    private final String id = "revision_123";
    private final Instant creationDate = Instant.now();
    private final String authorName = "name";
    private final String authorEmail = "em@i.l";
    private final String commit = "my branch commit";
    private final Changeset changeset = new Changeset(id, Date.from(creationDate).getTime(), new Person(authorName, authorEmail), commit);

    private final JsonMockHttpResponse response = new JsonMockHttpResponse();

    @BeforeEach
    void prepareExistingChangeset() throws IOException {
      when(logCommandBuilder.getChangeset(id)).thenReturn(changeset);
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

    @Test
    void shouldNotReturnRevertLinkWithoutParent() throws URISyntaxException {
      when(repositoryService.isSupported(Command.REVERT)).thenReturn(true);
      executeRequest();

      assertThat(response.getContentAsJson().get("_links").get("revert")).isNullOrEmpty();
    }
  }

  @Test
  void shouldReturnNotFoundForNonExistingChangeset() throws Exception {
    MockHttpRequest request = MockHttpRequest
      .get(CHANGESET_URL + "abcd")
      .accept(VndMediaType.CHANGESET);

    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(404);
  }

  @Nested
  class Revert {

    @Mock(answer = Answers.RETURNS_SELF)
    private RevertCommandBuilder revertCommandBuilder;

    @BeforeEach
    void prepareRevertCommand() {
      when(repositoryService.getRevertCommand()).thenReturn(revertCommandBuilder);
    }

    @Test
    void shouldCallRevertCommand() throws URISyntaxException {
      when(repositoryService.getRevertCommand().execute()).thenReturn(RevertCommandResult.success("42"));
      JsonMockHttpRequest request = JsonMockHttpRequest
        .post(CHANGESET_URL + "23/revert")
        .contentType(VndMediaType.REVERT)
        .json("{'branch':'main','message':'revert message'}");

      JsonMockHttpResponse response = new JsonMockHttpResponse();
      dispatcher.invoke(request, response);

      verify(revertCommandBuilder).setRevision("23");
      verify(revertCommandBuilder).setMessage("revert message");
      verify(revertCommandBuilder).setBranch("main");
      verify(revertCommandBuilder).execute();
      assertThat(response.getStatus()).isEqualTo(201);
      assertThat(response.getContentAsJson().get("revision").asText()).isEqualTo("42");
      assertThat(response.getOutputHeaders().getFirst("Location")).hasToString("/v2/repositories/space/repo/changesets/42");
    }

    @Test
    void shouldCallRevertCommandForDefaultMessage() throws URISyntaxException {
      when(repositoryService.getRevertCommand().execute()).thenReturn(RevertCommandResult.success("42"));
      JsonMockHttpRequest request = JsonMockHttpRequest
        .post(CHANGESET_URL + "23/revert")
        .contentType(VndMediaType.REVERT)
        .json("{'branch':'main'}");

      MockHttpResponse response = new MockHttpResponse();
      dispatcher.invoke(request, response);

      verify(revertCommandBuilder).setRevision("23");
      verify(revertCommandBuilder, never()).setMessage(anyString());
      verify(revertCommandBuilder).setBranch("main");
      verify(revertCommandBuilder).execute();
    }

    @Test
    void shouldCallRevertCommandForDefaultBranch() throws URISyntaxException {
      when(repositoryService.getRevertCommand().execute()).thenReturn(RevertCommandResult.success("42"));
      JsonMockHttpRequest request = JsonMockHttpRequest
        .post(CHANGESET_URL + "23/revert")
        .contentType(VndMediaType.REVERT)
        .json("{'message':'revert message'}");

      MockHttpResponse response = new MockHttpResponse();
      dispatcher.invoke(request, response);

      verify(revertCommandBuilder).setRevision("23");
      verify(revertCommandBuilder).setMessage("revert message");
      verify(revertCommandBuilder, never()).setBranch(anyString());
      verify(revertCommandBuilder).execute();
    }

    @Test
    void shouldFailWithoutPushPermission() throws URISyntaxException {
      doThrow(new UnauthorizedException("push"))
        .when(subject).checkPermission("repository:push:repoId");
      JsonMockHttpRequest request = JsonMockHttpRequest
        .post(CHANGESET_URL + "23/revert")
        .contentType(VndMediaType.REVERT)
        .json("{'branch':'main','message':'revert message'}");

      MockHttpResponse response = new MockHttpResponse();
      dispatcher.invoke(request, response);

      verify(revertCommandBuilder, never()).execute();
      assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void shouldFailWithConflicts() throws URISyntaxException {
      when(repositoryService.getRevertCommand().execute())
        .thenReturn(RevertCommandResult.failure(List.of("file/with/conflict")));

      JsonMockHttpRequest request = JsonMockHttpRequest
        .post(CHANGESET_URL + "23/revert")
        .contentType(VndMediaType.REVERT)
        .json("{}");

      MockHttpResponse response = new MockHttpResponse();
      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(409);
    }
  }
}
