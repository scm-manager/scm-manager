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

import com.google.inject.util.Providers;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.ContextEntry;
import sonia.scm.NotFoundException;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Person;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.LogCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.web.RestDispatcher;
import sonia.scm.web.VndMediaType;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
@Slf4j
public class FileHistoryResourceTest extends RepositoryTestBase {

  public static final String FILE_HISTORY_PATH = "space/repo/history/";
  public static final String FILE_HISTORY_URL = "/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + FILE_HISTORY_PATH;

  private final URI baseUri = URI.create("/");
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @Mock
  private RepositoryServiceFactory serviceFactory;

  @Mock
  private RepositoryService service;

  @Mock
  private LogCommandBuilder logCommandBuilder;

  private FileHistoryCollectionToDtoMapper fileHistoryCollectionToDtoMapper;

  @InjectMocks
  private DefaultChangesetToChangesetDtoMapperImpl changesetToChangesetDtoMapper;

  private FileHistoryRootResource fileHistoryRootResource;

  private RestDispatcher dispatcher = new RestDispatcher();

  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);

  @Before
  public void prepareEnvironment() {
    fileHistoryCollectionToDtoMapper = new FileHistoryCollectionToDtoMapper(changesetToChangesetDtoMapper, resourceLinks);
    fileHistoryRootResource = new FileHistoryRootResource(serviceFactory, fileHistoryCollectionToDtoMapper);
    super.fileHistoryRootResource = Providers.of(fileHistoryRootResource);
    dispatcher.addSingletonResource(getRepositoryRootResource());
    when(serviceFactory.create(new NamespaceAndName("space", "repo"))).thenReturn(service);
    when(serviceFactory.create(any(Repository.class))).thenReturn(service);
    when(service.getRepository()).thenReturn(new Repository("repoId", "git", "space", "repo"));
    ExceptionWithContextToErrorDtoMapperImpl mapper = new ExceptionWithContextToErrorDtoMapperImpl();
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
  public void shouldGetFileHistory() throws Exception {
    String id = "revision_123";
    String path = "root_dir/sub_dir/file-to-inspect.txt";
    Instant creationDate = Instant.now();
    String authorName = "name";
    String authorEmail = "em@i.l";
    String commit = "my branch commit";
    ChangesetPagingResult changesetPagingResult = mock(ChangesetPagingResult.class);
    List<Changeset> changesetList = Lists.newArrayList(new Changeset(id, Date.from(creationDate).getTime(), new Person(authorName, authorEmail), commit));
    when(changesetPagingResult.getChangesets()).thenReturn(changesetList);
    when(changesetPagingResult.getTotal()).thenReturn(1);
    when(logCommandBuilder.setPagingStart(anyInt())).thenReturn(logCommandBuilder);
    when(logCommandBuilder.setPagingLimit(anyInt())).thenReturn(logCommandBuilder);
    when(logCommandBuilder.setStartChangeset(eq(id))).thenReturn(logCommandBuilder);
    when(logCommandBuilder.setPath(eq(path))).thenReturn(logCommandBuilder);
    when(logCommandBuilder.getChangesets()).thenReturn(changesetPagingResult);
    MockHttpRequest request = MockHttpRequest
      .get(FILE_HISTORY_URL + id + "/" + path)
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
  public void shouldGet404OnMissingRepository() throws URISyntaxException {
    when(serviceFactory.create(any(NamespaceAndName.class))).thenThrow(new NotFoundException("Text", "x"));
    MockHttpRequest request = MockHttpRequest
      .get(FILE_HISTORY_URL + "revision/a.txt")
      .accept(VndMediaType.CHANGESET_COLLECTION);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertEquals(404, response.getStatus());
  }

  @Test
  public void shouldGet404OnMissingRevision() throws Exception {
    String id = "revision_123";
    String path = "root_dir/sub_dir/file-to-inspect.txt";

    when(logCommandBuilder.setPagingStart(anyInt())).thenReturn(logCommandBuilder);
    when(logCommandBuilder.setPagingLimit(anyInt())).thenReturn(logCommandBuilder);
    when(logCommandBuilder.setStartChangeset(eq(id))).thenReturn(logCommandBuilder);
    when(logCommandBuilder.setPath(eq(path))).thenReturn(logCommandBuilder);
    when(logCommandBuilder.getChangesets()).thenThrow(new NotFoundException("Text", "x"));

    MockHttpRequest request = MockHttpRequest
      .get(FILE_HISTORY_URL + id + "/" + path)
      .accept(VndMediaType.CHANGESET_COLLECTION);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertEquals(404, response.getStatus());
  }

  @Test
  public void shouldGet500OnInternalRepositoryException() throws Exception {
    String id = "revision_123";
    String path = "root_dir/sub_dir/file-to-inspect.txt";

    when(logCommandBuilder.setPagingStart(anyInt())).thenReturn(logCommandBuilder);
    when(logCommandBuilder.setPagingLimit(anyInt())).thenReturn(logCommandBuilder);
    when(logCommandBuilder.setStartChangeset(eq(id))).thenReturn(logCommandBuilder);
    when(logCommandBuilder.setPath(eq(path))).thenReturn(logCommandBuilder);
    when(logCommandBuilder.getChangesets()).thenThrow(new InternalRepositoryException(ContextEntry.ContextBuilder.noContext(), "", new RuntimeException()));

    MockHttpRequest request = MockHttpRequest
      .get(FILE_HISTORY_URL + id + "/" + path)
      .accept(VndMediaType.CHANGESET_COLLECTION);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertEquals(500, response.getStatus());
  }

  @Test
  public void shouldGet404OnNullChangesets() throws Exception {
    String id = "revision_123";
    String path = "root_dir/sub_dir/file-to-inspect.txt";

    when(logCommandBuilder.setPagingStart(anyInt())).thenReturn(logCommandBuilder);
    when(logCommandBuilder.setPagingLimit(anyInt())).thenReturn(logCommandBuilder);
    when(logCommandBuilder.setStartChangeset(eq(id))).thenReturn(logCommandBuilder);
    when(logCommandBuilder.setPath(eq(path))).thenReturn(logCommandBuilder);
    when(logCommandBuilder.getChangesets()).thenReturn(null);

    MockHttpRequest request = MockHttpRequest
      .get(FILE_HISTORY_URL + id + "/" + path)
      .accept(VndMediaType.CHANGESET_COLLECTION);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertEquals(404, response.getStatus());
  }
}
