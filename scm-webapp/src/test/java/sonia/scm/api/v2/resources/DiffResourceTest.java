package sonia.scm.api.v2.resources;


import com.google.inject.util.Providers;
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
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.NotFoundException;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.DiffCommandBuilder;
import sonia.scm.repository.api.DiffFormat;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.util.CRLFInjectionException;
import sonia.scm.web.RestDispatcher;
import sonia.scm.web.VndMediaType;

import javax.ws.rs.core.Response;
import java.net.URISyntaxException;
import java.util.Arrays;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
@Slf4j
public class DiffResourceTest extends RepositoryTestBase {


  public static final String DIFF_PATH = "space/repo/diff/";
  public static final String DIFF_URL = "/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + DIFF_PATH;

  private RestDispatcher dispatcher = new RestDispatcher();

  @Mock
  private RepositoryServiceFactory serviceFactory;

  @Mock
  private RepositoryService service;

  @Mock
  private DiffCommandBuilder diffCommandBuilder;

  private DiffRootResource diffRootResource;


  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);


  @Before
  public void prepareEnvironment() {
    diffRootResource = new DiffRootResource(serviceFactory);
    super.diffRootResource = Providers.of(diffRootResource);
    dispatcher.addSingletonResource(getRepositoryRootResource());
    when(serviceFactory.create(new NamespaceAndName("space", "repo"))).thenReturn(service);
    when(serviceFactory.create(any(Repository.class))).thenReturn(service);
    when(service.getRepository()).thenReturn(new Repository("repoId", "git", "space", "repo"));
    ExceptionWithContextToErrorDtoMapperImpl mapper = new ExceptionWithContextToErrorDtoMapperImpl();
    dispatcher.registerException(CRLFInjectionException.class, Response.Status.BAD_REQUEST);
    when(service.getDiffCommand()).thenReturn(diffCommandBuilder);
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
    when(diffCommandBuilder.setRevision(anyString())).thenReturn(diffCommandBuilder);
    when(diffCommandBuilder.setFormat(any())).thenReturn(diffCommandBuilder);
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
    when(diffCommandBuilder.setRevision(anyString())).thenReturn(diffCommandBuilder);
    when(diffCommandBuilder.setFormat(any())).thenReturn(diffCommandBuilder);
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
    when(diffCommandBuilder.setRevision(anyString())).thenReturn(diffCommandBuilder);
    when(diffCommandBuilder.setFormat(any())).thenReturn(diffCommandBuilder);
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
    when(diffCommandBuilder.setRevision(anyString())).thenReturn(diffCommandBuilder);
    when(diffCommandBuilder.setFormat(any())).thenReturn(diffCommandBuilder);
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
    when(diffCommandBuilder.setRevision(anyString())).thenReturn(diffCommandBuilder);
    when(diffCommandBuilder.setFormat(any())).thenReturn(diffCommandBuilder);
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
