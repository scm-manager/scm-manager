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


import de.otto.edison.hal.Links;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
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
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.NotFoundException;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Person;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.DiffCommandBuilder;
import sonia.scm.repository.api.DiffResult;
import sonia.scm.repository.api.DiffResultCommandBuilder;
import sonia.scm.repository.api.LogCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.util.CRLFInjectionException;
import sonia.scm.web.RestDispatcher;
import sonia.scm.web.VndMediaType;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sonia.scm.repository.api.DiffFormat.NATIVE;

@RunWith(MockitoJUnitRunner.Silent.class)
@Slf4j
public class IncomingRootResourceTest extends RepositoryTestBase {

  public static final String INCOMING_PATH = "space/repo/incoming/";
  public static final String INCOMING_CHANGESETS_URL = "/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + INCOMING_PATH;
  public static final String INCOMING_DIFF_URL = "/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + INCOMING_PATH;
  public static final Repository REPOSITORY = new Repository("repoId", "git", "space", "repo");

  private RestDispatcher dispatcher = new RestDispatcher();

  private final URI baseUri = URI.create("/");
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @Mock
  private RepositoryServiceFactory serviceFactory;

  @Mock
  private RepositoryService repositoryService;

  @Mock
  private LogCommandBuilder logCommandBuilder;

  @Mock(answer = Answers.RETURNS_SELF)
  private DiffCommandBuilder diffCommandBuilder;
  @Mock(answer = Answers.RETURNS_SELF)
  private DiffResultCommandBuilder diffResultCommandBuilder;

  @Mock
  private DiffResultToDiffResultDtoMapper diffResultToDiffResultDtoMapper;

  private IncomingChangesetCollectionToDtoMapper incomingChangesetCollectionToDtoMapper;

  @InjectMocks
  private DefaultChangesetToChangesetDtoMapperImpl changesetToChangesetDtoMapper;

  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);


  @Before
  public void prepareEnvironment() {
    incomingChangesetCollectionToDtoMapper = new IncomingChangesetCollectionToDtoMapper(changesetToChangesetDtoMapper, resourceLinks);
    incomingRootResource = new IncomingRootResource(serviceFactory, incomingChangesetCollectionToDtoMapper, diffResultToDiffResultDtoMapper);
    dispatcher.addSingletonResource(getRepositoryRootResource());
    when(serviceFactory.create(new NamespaceAndName("space", "repo"))).thenReturn(repositoryService);
    when(serviceFactory.create(REPOSITORY)).thenReturn(repositoryService);
    when(repositoryService.getRepository()).thenReturn(REPOSITORY);
    when(repositoryService.getLogCommand()).thenReturn(logCommandBuilder);
    when(repositoryService.getDiffCommand()).thenReturn(diffCommandBuilder);
    when(repositoryService.getDiffResultCommand()).thenReturn(diffResultCommandBuilder);
    dispatcher.registerException(CRLFInjectionException.class, Response.Status.BAD_REQUEST);
    subjectThreadState.bind();
    ThreadContext.bind(subject);
    when(subject.isPermitted(any(String.class))).thenReturn(true);
  }

  @After
  public void cleanupContext() {
    ThreadContext.unbindSubject();
  }

  @Test
  public void shouldGetIncomingChangesets() throws Exception {
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
    when(logCommandBuilder.setStartChangeset(anyString())).thenReturn(logCommandBuilder);
    when(logCommandBuilder.setAncestorChangeset(anyString())).thenReturn(logCommandBuilder);
    when(logCommandBuilder.getChangesets()).thenReturn(changesetPagingResult);
    MockHttpRequest request = MockHttpRequest
      .get(INCOMING_CHANGESETS_URL + "src_changeset_id/target_changeset_id/changesets")
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
  public void shouldGetSinglePageOfIncomingChangesets() throws Exception {
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
    when(logCommandBuilder.setStartChangeset(anyString())).thenReturn(logCommandBuilder);
    when(logCommandBuilder.setAncestorChangeset(anyString())).thenReturn(logCommandBuilder);
    when(logCommandBuilder.getChangesets()).thenReturn(changesetPagingResult);
    MockHttpRequest request = MockHttpRequest
      .get(INCOMING_CHANGESETS_URL + "src_changeset_id/target_changeset_id/changesets?page=2")
      .accept(VndMediaType.CHANGESET_COLLECTION);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(200, response.getStatus());
    assertTrue(response.getContentAsString().contains(String.format("\"id\":\"%s\"", id)));
    assertTrue(response.getContentAsString().contains(String.format("\"name\":\"%s\"", authorName)));
    assertTrue(response.getContentAsString().contains(String.format("\"mail\":\"%s\"", authorEmail)));
    assertTrue(response.getContentAsString().contains(String.format("\"description\":\"%s\"", commit)));
  }

  @Test
  public void shouldGetDiffs() throws Exception {
    when(diffCommandBuilder.retrieveContent()).thenReturn(output -> {});
    MockHttpRequest request = MockHttpRequest
      .get(INCOMING_DIFF_URL + "src_changeset_id/target_changeset_id/diff")
      .accept(VndMediaType.DIFF);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus())
      .isEqualTo(200);
    String expectedHeader = "Content-Disposition";
    String expectedValue = "attachment; filename=\"repo-src_changeset_id.diff\"; filename*=utf-8''repo-src_changeset_id.diff";
    assertThat(response.getOutputHeaders().containsKey(expectedHeader)).isTrue();
    assertThat((String) response.getOutputHeaders().get("Content-Disposition").get(0))
      .contains(expectedValue);
    verify(diffCommandBuilder).setRevision("src_changeset_id");
    verify(diffCommandBuilder).setAncestorChangeset("target_changeset_id");
    verify(diffCommandBuilder).setFormat(NATIVE);
  }

  @Test
  public void shouldGetParsedDiffs() throws Exception {
    DiffResult diffResult = mock(DiffResult.class);
    when(diffResultCommandBuilder.getDiffResult()).thenReturn(diffResult);
    when(diffResultToDiffResultDtoMapper.mapForIncoming(REPOSITORY, diffResult, "src_changeset_id", "target_changeset_id"))
    .thenReturn(new DiffResultDto(Links.linkingTo().self("http://self").build()));

    MockHttpRequest request = MockHttpRequest
      .get(INCOMING_DIFF_URL + "src_changeset_id/target_changeset_id/diff/parsed")
      .accept(VndMediaType.DIFF_PARSED);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus())
      .isEqualTo(200);
    assertThat(response.getContentAsString())
      .contains("\"self\":{\"href\":\"http://self\"}");
    verify(diffResultCommandBuilder).setRevision("src_changeset_id");
    verify(diffResultCommandBuilder).setAncestorChangeset("target_changeset_id");
  }

  @Test
  public void shouldGetParsedDiffsWithLimit() throws Exception {
    DiffResult diffResult = mock(DiffResult.class);
    when(diffResultCommandBuilder.getDiffResult()).thenReturn(diffResult);
    when(diffResultToDiffResultDtoMapper.mapForIncoming(REPOSITORY, diffResult, "src_changeset_id", "target_changeset_id"))
    .thenReturn(new DiffResultDto(Links.linkingTo().self("http://self").build()));

    MockHttpRequest request = MockHttpRequest
      .get(INCOMING_DIFF_URL + "src_changeset_id/target_changeset_id/diff/parsed?limit=42")
      .accept(VndMediaType.DIFF_PARSED);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    verify(diffResultCommandBuilder).setLimit(42);
  }

  @Test
  public void shouldGetParsedDiffsWithOffset() throws Exception {
    DiffResult diffResult = mock(DiffResult.class);
    when(diffResultCommandBuilder.getDiffResult()).thenReturn(diffResult);
    when(diffResultToDiffResultDtoMapper.mapForIncoming(REPOSITORY, diffResult, "src_changeset_id", "target_changeset_id"))
    .thenReturn(new DiffResultDto(Links.linkingTo().self("http://self").build()));

    MockHttpRequest request = MockHttpRequest
      .get(INCOMING_DIFF_URL + "src_changeset_id/target_changeset_id/diff/parsed?offset=42")
      .accept(VndMediaType.DIFF_PARSED);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    verify(diffResultCommandBuilder).setOffset(42);
  }

  @Test
  public void shouldGet404OnMissingRepository() throws URISyntaxException {
    when(serviceFactory.create(any(NamespaceAndName.class))).thenThrow(new NotFoundException("Text", "x"));
    MockHttpRequest request = MockHttpRequest
      .get(INCOMING_DIFF_URL + "src_changeset_id/target_changeset_id/diff")
      .accept(VndMediaType.DIFF);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(404, response.getStatus());
  }

  @Test
  public void shouldGet404OnMissingRevision() throws Exception {
    when(diffCommandBuilder.retrieveContent()).thenThrow(new NotFoundException("Text", "x"));

    MockHttpRequest request = MockHttpRequest
      .get(INCOMING_DIFF_URL + "src_changeset_id/target_changeset_id/diff")
      .accept(VndMediaType.DIFF);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(404, response.getStatus());
  }

  @Test
  public void shouldGet400OnCrlfInjection() throws Exception {
    when(diffCommandBuilder.retrieveContent()).thenThrow(new NotFoundException("Text", "x"));
    MockHttpRequest request = MockHttpRequest
      .get(INCOMING_DIFF_URL + "ny%0D%0ASet-cookie:%20Tamper=3079675143472450634/ny%0D%0ASet-cookie:%20Tamper=3079675143472450634/diff")
      .accept(VndMediaType.DIFF);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(400, response.getStatus());
    assertThat(response.getContentAsString()).contains("parameter contains an illegal character");
  }

  @Test
  public void shouldGet400OnUnknownFormat() throws Exception {
    when(diffCommandBuilder.retrieveContent()).thenThrow(new NotFoundException("Test", "test"));
    MockHttpRequest request = MockHttpRequest
      .get(INCOMING_DIFF_URL + "src_changeset_id/target_changeset_id/diff?format=Unknown")
      .accept(VndMediaType.DIFF);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(400, response.getStatus());
  }


}
