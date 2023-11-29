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

import jakarta.ws.rs.core.MediaType;
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
import sonia.scm.repository.Branch;
import sonia.scm.repository.Branches;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Person;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.BranchCommandBuilder;
import sonia.scm.repository.api.BranchesCommandBuilder;
import sonia.scm.repository.api.LogCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
@Slf4j
public class BranchRootResourceTest extends RepositoryTestBase {

  public static final String BRANCH_PATH = "space/repo/branches/master";
  public static final String BRANCH_URL = "/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + BRANCH_PATH;
  public static final String REVISION = "revision";

  private RestDispatcher dispatcher = new RestDispatcher();

  private final URI baseUri = URI.create("/");
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private RepositoryService service;
  @Mock
  private BranchesCommandBuilder branchesCommandBuilder;
  @Mock
  private BranchCommandBuilder branchCommandBuilder;

  @Mock
  private LogCommandBuilder logCommandBuilder;

  @InjectMocks
  private BranchToBranchDtoMapperImpl branchToDtoMapper;

  private BranchChangesetCollectionToDtoMapper changesetCollectionToDtoMapper;

  @Mock
  private BranchCollectionToDtoMapper branchCollectionToDtoMapper;

  @Mock
  private ChangesetToParentDtoMapper changesetToParentDtoMapper;

  @Mock
  private TagCollectionToDtoMapper tagCollectionToDtoMapper;

  @InjectMocks
  private DefaultChangesetToChangesetDtoMapperImpl changesetToChangesetDtoMapper;

  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);


  @Before
  public void prepareEnvironment() {
    changesetCollectionToDtoMapper = new BranchChangesetCollectionToDtoMapper(changesetToChangesetDtoMapper, resourceLinks);
    BranchCollectionToDtoMapper branchCollectionToDtoMapper = new BranchCollectionToDtoMapper(branchToDtoMapper, resourceLinks);
    branchRootResource = new BranchRootResource(serviceFactory, branchToDtoMapper, branchCollectionToDtoMapper, changesetCollectionToDtoMapper, resourceLinks);
    dispatcher.addSingletonResource(getRepositoryRootResource());
    when(serviceFactory.create(new NamespaceAndName("space", "repo"))).thenReturn(service);
    when(serviceFactory.create(any(Repository.class))).thenReturn(service);
    when(service.getRepository()).thenReturn(new Repository("repoId", "git", "space", "repo"));

    when(service.getBranchesCommand()).thenReturn(branchesCommandBuilder);
    when(service.getBranchCommand()).thenReturn(branchCommandBuilder);
    when(service.getLogCommand()).thenReturn(logCommandBuilder);
    subjectThreadState.bind();
    ThreadContext.bind(subject);
    when(subject.isPermitted(any(String.class))).thenReturn(true);
  }

  @After
  public void cleanupContext() {
    ThreadContext.unbindSubject();
  }

  @Test
  public void shouldHandleMissingBranch() throws Exception {
    when(branchesCommandBuilder.getBranches()).thenReturn(new Branches());

    MockHttpRequest request = MockHttpRequest.get(BRANCH_URL);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(404, response.getStatus());
    MediaType contentType = (MediaType) response.getOutputHeaders().getFirst("Content-Type");
    assertThat(response.getContentAsString()).contains("branch", "master", "space/repo");
  }

  @Test
  public void shouldFindExistingBranch() throws Exception {
    when(branchesCommandBuilder.getBranches()).thenReturn(new Branches(createBranch("master")));

    MockHttpRequest request = MockHttpRequest.get(BRANCH_URL);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(200, response.getStatus());
    log.info("Response :{}", response.getContentAsString());
    assertTrue(response.getContentAsString().contains("\"revision\":\"revision\""));
  }

  @Test
  public void shouldFindHistory() throws Exception {
    Instant creationDate = Instant.now();
    String authorName = "name";
    String authorEmail = "em@i.l";
    String commit = "my branch commit";
    ChangesetPagingResult changesetPagingResult = mock(ChangesetPagingResult.class);
    List<Changeset> changesetList = Lists.newArrayList(new Changeset(REVISION, Date.from(creationDate).getTime(), new Person(authorName, authorEmail), commit));
    when(changesetPagingResult.getChangesets()).thenReturn(changesetList);
    when(changesetPagingResult.getTotal()).thenReturn(1);
    when(logCommandBuilder.setPagingStart(anyInt())).thenReturn(logCommandBuilder);
    when(logCommandBuilder.setPagingLimit(anyInt())).thenReturn(logCommandBuilder);
    when(logCommandBuilder.setBranch(anyString())).thenReturn(logCommandBuilder);
    when(logCommandBuilder.getChangesets()).thenReturn(changesetPagingResult);
    Branches branches = mock(Branches.class);
    List<Branch> branchList = Lists.newArrayList(createBranch("master"));
    when(branches.getBranches()).thenReturn(branchList);
    when(branchesCommandBuilder.getBranches()).thenReturn(branches);
    MockHttpRequest request = MockHttpRequest.get(BRANCH_URL + "/changesets/");
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertEquals(200, response.getStatus());
    log.info("Response :{}", response.getContentAsString());
    assertThat(response.getContentAsString()).contains(String.format("\"id\":\"%s\"", REVISION));
    assertThat(response.getContentAsString()).contains(String.format("\"name\":\"%s\"", authorName));
    assertThat(response.getContentAsString()).contains(String.format("\"mail\":\"%s\"", authorEmail));
    assertThat(response.getContentAsString()).contains(String.format("\"description\":\"%s\"", commit));
  }

  @Test
  public void shouldCreateNewBranch() throws Exception {
    when(branchesCommandBuilder.getBranches()).thenReturn(new Branches());
    when(branchCommandBuilder.branch("new_branch")).thenReturn(createBranch("new_branch"));

    MockHttpRequest request = MockHttpRequest
      .post("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/branches/")
      .content("{\"name\": \"new_branch\"}".getBytes())
      .contentType(VndMediaType.BRANCH_REQUEST);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(201, response.getStatus());
    assertEquals(
      URI.create("/v2/repositories/space/repo/branches/new_branch"),
      response.getOutputHeaders().getFirst("Location"));
  }

  @Test
  public void shouldCreateNewBranchWithParent() throws Exception {
    when(branchesCommandBuilder.getBranches()).thenReturn(new Branches(createBranch("existing_branch")));
    when(branchCommandBuilder.from("existing_branch")).thenReturn(branchCommandBuilder);
    when(branchCommandBuilder.branch("new_branch")).thenReturn(createBranch("new_branch"));

    MockHttpRequest request = MockHttpRequest
      .post("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/branches/")
      .content("{\"name\": \"new_branch\",\"parent\": \"existing_branch\"}".getBytes())
      .contentType(VndMediaType.BRANCH_REQUEST);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(201, response.getStatus());
    assertEquals(
      URI.create("/v2/repositories/space/repo/branches/new_branch"),
      response.getOutputHeaders().getFirst("Location"));
    verify(branchCommandBuilder).from("existing_branch");
  }

  @Test
  public void shouldNotCreateExistingBranchAgain() throws Exception {
    when(branchesCommandBuilder.getBranches()).thenReturn(new Branches(createBranch("existing_branch")));

    MockHttpRequest request = MockHttpRequest
      .post("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/branches/")
      .content("{\"name\": \"existing_branch\"}".getBytes())
      .contentType(VndMediaType.BRANCH_REQUEST);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(409, response.getStatus());
    verify(branchCommandBuilder, never()).branch(anyString());
  }

  @Test
  public void shouldNotCreateBranchIfDirectoryAsBranchAlreadyExists() throws Exception {
    when(branchesCommandBuilder.getBranches()).thenReturn(new Branches(createBranch("existing_branch")));

    MockHttpRequest request = MockHttpRequest
      .post("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/branches/")
      .content("{\"name\": \"existing_branch/abc\"}".getBytes())
      .contentType(VndMediaType.BRANCH_REQUEST);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(409, response.getStatus());
    verify(branchCommandBuilder, never()).branch(anyString());
  }

  @Test
  public void shouldNotCreateBranchIfBranchWithDirectoryAlreadyExists() throws Exception {
    when(branchesCommandBuilder.getBranches()).thenReturn(new Branches(createBranch("existing_branch/abc")));

    MockHttpRequest request = MockHttpRequest
      .post("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/branches/")
      .content("{\"name\": \"existing_branch\"}".getBytes())
      .contentType(VndMediaType.BRANCH_REQUEST);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(409, response.getStatus());
    verify(branchCommandBuilder, never()).branch(anyString());
  }

  @Test
  public void shouldFailForMissingParentBranch() throws Exception {
    when(branchesCommandBuilder.getBranches()).thenReturn(new Branches(createBranch("existing_branch")));

    MockHttpRequest request = MockHttpRequest
      .post("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/branches/")
      .content("{\"name\": \"new_branch\",\"parent\": \"no_such_branch\"}".getBytes())
      .contentType(VndMediaType.BRANCH_REQUEST);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(404, response.getStatus());
    verify(branchCommandBuilder, never()).branch(anyString());
  }

  @Test
  public void shouldNotDeleteBranchIfNotPermitted() throws IOException, URISyntaxException {
    doThrow(AuthorizationException.class).when(subject).checkPermission("repository:push:repoId");
    when(branchesCommandBuilder.getBranches()).thenReturn(new Branches(Branch.normalBranch("suspicious", "0")));

    MockHttpRequest request = MockHttpRequest
      .delete("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/branches/suspicious");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(403, response.getStatus());
    verify(branchCommandBuilder, never()).delete("suspicious");
  }

  @Test
  public void shouldNotDeleteDefaultBranch() throws IOException, URISyntaxException {
    when(branchesCommandBuilder.getBranches()).thenReturn(new Branches(Branch.defaultBranch("main", "0")));

    MockHttpRequest request = MockHttpRequest
      .delete("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/branches/main");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(400, response.getStatus());
  }

  @Test
  public void shouldDeleteBranch() throws IOException, URISyntaxException {
    when(branchesCommandBuilder.getBranches()).thenReturn(new Branches(Branch.normalBranch("suspicious", "0")));

    MockHttpRequest request = MockHttpRequest
      .delete("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/branches/suspicious");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(204, response.getStatus());
    verify(branchCommandBuilder).delete("suspicious");
  }

  @Test
  public void shouldAnswer204IfNothingWasDeleted() throws IOException, URISyntaxException {
    when(branchesCommandBuilder.getBranches()).thenReturn(new Branches());

    MockHttpRequest request = MockHttpRequest
      .delete("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/branches/suspicious");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(204, response.getStatus());
    verify(branchCommandBuilder, never()).delete(anyString());
  }

  private Branch createBranch(String existing_branch) {
    return Branch.normalBranch(existing_branch, REVISION);
  }
}
