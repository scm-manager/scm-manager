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
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.NotFoundException;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.DiffCommandBuilder;
import sonia.scm.repository.api.DiffFormat;
import sonia.scm.repository.api.DiffResult;
import sonia.scm.repository.api.DiffResultCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.util.CRLFInjectionException;
import sonia.scm.web.RestDispatcher;
import sonia.scm.web.VndMediaType;

import java.net.URISyntaxException;
import java.util.Arrays;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
@Slf4j
public class DiffResourceTest extends RepositoryTestBase {


  public static final String DIFF_PATH = "space/repo/diff/";
  public static final String DIFF_URL = "/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + DIFF_PATH;
  public static final Repository REPOSITORY = new Repository("repoId", "git", "space", "repo");

  private RestDispatcher dispatcher = new RestDispatcher();

  @Mock
  private RepositoryServiceFactory serviceFactory;

  @Mock
  private RepositoryService service;

  @Mock(answer = Answers.RETURNS_SELF)
  private DiffCommandBuilder diffCommandBuilder;
  @Mock(answer = Answers.RETURNS_SELF)
  private DiffResultCommandBuilder diffResultCommandBuilder;

  @Mock
  private DiffResultToDiffResultDtoMapper diffResultToDiffResultDtoMapper;

  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);


  @Before
  public void prepareEnvironment() {
    diffRootResource = new DiffRootResource(serviceFactory, diffResultToDiffResultDtoMapper);
    dispatcher.addSingletonResource(getRepositoryRootResource());
    when(serviceFactory.create(new NamespaceAndName("space", "repo"))).thenReturn(service);
    when(serviceFactory.create(any(Repository.class))).thenReturn(service);
    when(service.getRepository()).thenReturn(REPOSITORY);
    ExceptionWithContextToErrorDtoMapperImpl mapper = new ExceptionWithContextToErrorDtoMapperImpl();
    dispatcher.registerException(CRLFInjectionException.class, Response.Status.BAD_REQUEST);
    when(service.getDiffCommand()).thenReturn(diffCommandBuilder);
    when(service.getDiffResultCommand()).thenReturn(diffResultCommandBuilder);
    subjectThreadState.bind();
    ThreadContext.bind(subject);
    when(subject.isPermitted(any(String.class))).thenReturn(true);
  }

  @After
  public void cleanupContext() {
    ThreadContext.unbindSubject();
  }

  @Test
  public void shouldGetDiffs() throws Exception {
    when(diffCommandBuilder.retrieveContent()).thenReturn(output -> {});
    MockHttpRequest request = MockHttpRequest
      .get(DIFF_URL + "revision")
      .accept(VndMediaType.DIFF);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus())
      .isEqualTo(200);
    String expectedHeader = "Content-Disposition";
    String expectedValue = "attachment; filename=\"repo-revision.diff\"; filename*=utf-8''repo-revision.diff";
    assertThat(response.getOutputHeaders().containsKey(expectedHeader)).isTrue();
    assertThat((String) response.getOutputHeaders().get("Content-Disposition").get(0))
      .contains(expectedValue);
  }

  @Test
  public void shouldGetParsedDiffs() throws Exception {
    DiffResult diffResult = mock(DiffResult.class);
    when(diffResultCommandBuilder.getDiffResult()).thenReturn(diffResult);
    when(diffResultToDiffResultDtoMapper.mapForRevision(REPOSITORY, diffResult, "revision"))
      .thenReturn(new DiffResultDto(Links.linkingTo().self("http://self").build()));
    MockHttpRequest request = MockHttpRequest
      .get(DIFF_URL + "revision/parsed")
      .accept(VndMediaType.DIFF_PARSED);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus())
      .isEqualTo(200);
    assertThat(response.getContentAsString())
      .contains("\"self\":{\"href\":\"http://self\"}");
  }

  @Test
  public void shouldGetParsedDiffsWithOffset() throws Exception {
    DiffResult diffResult = mock(DiffResult.class);
    when(diffResultCommandBuilder.getDiffResult()).thenReturn(diffResult);
    when(diffResultToDiffResultDtoMapper.mapForRevision(REPOSITORY, diffResult, "revision"))
      .thenReturn(new DiffResultDto(Links.linkingTo().self("http://self").build()));
    MockHttpRequest request = MockHttpRequest
      .get(DIFF_URL + "revision/parsed?offset=42")
      .accept(VndMediaType.DIFF_PARSED);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    verify(diffResultCommandBuilder).setOffset(42);
  }

  @Test
  public void shouldGetParsedDiffsWithLimit() throws Exception {
    DiffResult diffResult = mock(DiffResult.class);
    when(diffResultCommandBuilder.getDiffResult()).thenReturn(diffResult);
    when(diffResultToDiffResultDtoMapper.mapForRevision(REPOSITORY, diffResult, "revision"))
      .thenReturn(new DiffResultDto(Links.linkingTo().self("http://self").build()));
    MockHttpRequest request = MockHttpRequest
      .get(DIFF_URL + "revision/parsed?limit=42")
      .accept(VndMediaType.DIFF_PARSED);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    verify(diffResultCommandBuilder).setLimit(42);
  }

  @Test
  public void shouldGet404OnMissingRepository() throws URISyntaxException {
    when(serviceFactory.create(any(NamespaceAndName.class))).thenThrow(new NotFoundException("Text", "x"));
    MockHttpRequest request = MockHttpRequest
      .get(DIFF_URL + "revision")
      .accept(VndMediaType.DIFF);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertEquals(404, response.getStatus());
  }

  @Test
  public void shouldGet404OnMissingRevision() throws Exception {
    when(diffCommandBuilder.retrieveContent()).thenThrow(new NotFoundException("Text", "x"));

    MockHttpRequest request = MockHttpRequest
      .get(DIFF_URL + "revision")
      .accept(VndMediaType.DIFF);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(404, response.getStatus());
  }

  @Test
  public void shouldGet400OnCrlfInjection() throws Exception {
    when(diffCommandBuilder.retrieveContent()).thenThrow(new NotFoundException("Text", "x"));

    MockHttpRequest request = MockHttpRequest
      .get(DIFF_URL + "ny%0D%0ASet-cookie:%20Tamper=3079675143472450634")
      .accept(VndMediaType.DIFF);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertEquals(400, response.getStatus());
  }

  @Test
  public void shouldGet400OnUnknownFormat() throws Exception {
    when(diffCommandBuilder.retrieveContent()).thenThrow(new NotFoundException("Test", "test"));

    MockHttpRequest request = MockHttpRequest
      .get(DIFF_URL + "revision?format=Unknown")
      .accept(VndMediaType.DIFF);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertEquals(400, response.getStatus());
  }

  @Test
  public void shouldAcceptDiffFormats() throws Exception {
    when(diffCommandBuilder.retrieveContent()).thenReturn(output -> {});

    Arrays.stream(DiffFormat.values()).map(DiffFormat::name).forEach(
      this::assertRequestOk
    );
  }

  private void assertRequestOk(String format) {
    MockHttpRequest request = null;
    try {
      request = MockHttpRequest
        .get(DIFF_URL + "revision?format=" + format)
        .accept(VndMediaType.DIFF);
    } catch (URISyntaxException e) {
      e.printStackTrace();
      fail("got exception: " + e);
    }
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertThat(response.getStatus())
      .withFailMessage("diff format from DiffFormat enum must be accepted: " + format)
      .isEqualTo(200);
  }
}
