package sonia.scm.api.v2.resources;


import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.util.ThreadState;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.api.rest.AuthorizationExceptionMapper;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryNotFoundException;
import sonia.scm.repository.RevisionNotFoundException;
import sonia.scm.repository.api.DiffCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.web.VndMediaType;

import java.net.URISyntaxException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
@Slf4j
public class DiffResourceTest {


  public static final String DIFF_PATH = "space/repo/diff/";
  public static final String DIFF_URL = "/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + DIFF_PATH;
  private final Dispatcher dispatcher = MockDispatcherFactory.createDispatcher();

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
  public void prepareEnvironment() throws Exception {
    diffRootResource = new DiffRootResource(serviceFactory);
    RepositoryRootResource repositoryRootResource = new RepositoryRootResource(MockProvider
      .of(new RepositoryResource(null, null, null, null, null,
        null, null, null, null, MockProvider.of(diffRootResource))), null);
    dispatcher.getRegistry().addSingletonResource(repositoryRootResource);
    when(serviceFactory.create(new NamespaceAndName("space", "repo"))).thenReturn(service);
    when(serviceFactory.create(any(Repository.class))).thenReturn(service);
    when(service.getRepository()).thenReturn(new Repository("repoId", "git", "space", "repo"));
    dispatcher.getProviderFactory().registerProvider(NotFoundExceptionMapper.class);
    dispatcher.getProviderFactory().registerProvider(AuthorizationExceptionMapper.class);
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
    when(diffCommandBuilder.retriveContent(any())).thenReturn(diffCommandBuilder);

    MockHttpRequest request = MockHttpRequest
      .get(DIFF_URL + "revision")
      .accept(VndMediaType.DIFF);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertEquals(200, response.getStatus());
    log.info("Response :{}", response.getContentAsString());
    assertThat(response.getStatus())
      .isEqualTo(200);
    assertThat(response.getContentAsString())
      .isNotNull();
    String expectedHeader = "Content-Disposition";
    String expectedValue = "attachment; filename=\"repo-revision.diff\"; filename*=utf-8''repo-revision.diff";
    assertThat(response.getOutputHeaders().containsKey(expectedHeader)).isTrue();
    assertThat((String) response.getOutputHeaders().get("Content-Disposition").get(0))
      .contains(expectedValue);
  }

  @Test
  public void shouldGet404OnMissingRepository() throws URISyntaxException, RepositoryNotFoundException {
    when(serviceFactory.create(any(NamespaceAndName.class))).thenThrow(RepositoryNotFoundException.class);
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
    when(diffCommandBuilder.retriveContent(any())).thenThrow(RevisionNotFoundException.class);

    MockHttpRequest request = MockHttpRequest
      .get(DIFF_URL + "revision")
      .accept(VndMediaType.DIFF);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertEquals(404, response.getStatus());
  }

}
