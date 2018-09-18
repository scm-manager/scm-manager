package sonia.scm.api.v2.resources;


import com.google.inject.util.Providers;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.util.ThreadState;
import org.assertj.core.util.Lists;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.api.rest.AuthorizationExceptionMapper;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Person;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.LogCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.web.VndMediaType;

import java.net.URI;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
@Slf4j
public class ChangesetRootResourceTest extends RepositoryTestBase {


  public static final String CHANGESET_PATH = "space/repo/changesets/";
  public static final String CHANGESET_URL = "/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + CHANGESET_PATH;
  private final Dispatcher dispatcher = MockDispatcherFactory.createDispatcher();

  private final URI baseUri = URI.create("/");
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @Mock
  private RepositoryServiceFactory serviceFactory;

  @Mock
  private RepositoryService repositoryService;

  @Mock
  private LogCommandBuilder logCommandBuilder;

  private ChangesetCollectionToDtoMapper changesetCollectionToDtoMapper;

  @InjectMocks
  private ChangesetToChangesetDtoMapperImpl changesetToChangesetDtoMapper;

  private ChangesetRootResource changesetRootResource;


  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);


  @Before
  public void prepareEnvironment() throws Exception {
    changesetCollectionToDtoMapper = new ChangesetCollectionToDtoMapper(changesetToChangesetDtoMapper, resourceLinks);
    changesetRootResource = new ChangesetRootResource(serviceFactory, changesetCollectionToDtoMapper, changesetToChangesetDtoMapper);
    super.changesetRootResource = Providers.of(changesetRootResource);
    dispatcher.getRegistry().addSingletonResource(getRepositoryRootResource());
    when(serviceFactory.create(new NamespaceAndName("space", "repo"))).thenReturn(repositoryService);
    when(serviceFactory.create(any(Repository.class))).thenReturn(repositoryService);
    when(repositoryService.getRepository()).thenReturn(new Repository("repoId", "git", "space", "repo"));
    dispatcher.getProviderFactory().registerProvider(NotFoundExceptionMapper.class);
    dispatcher.getProviderFactory().registerProvider(AuthorizationExceptionMapper.class);
    when(repositoryService.getLogCommand()).thenReturn(logCommandBuilder);
    subjectThreadState.bind();
    ThreadContext.bind(subject);
    when(subject.isPermitted(any(String.class))).thenReturn(true);
  }

  @After
  public void cleanupContext() {
    ThreadContext.unbindSubject();
  }

  @Test
  public void shouldGetChangeSets() throws Exception {
    String id = "revision_123";
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
  public void shouldGetChangeSet() throws Exception {
    String id = "revision_123";
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
    when(logCommandBuilder.setEndChangeset(anyString())).thenReturn(logCommandBuilder);
    when(logCommandBuilder.setStartChangeset(anyString())).thenReturn(logCommandBuilder);
    when(logCommandBuilder.getChangesets()).thenReturn(changesetPagingResult);
    MockHttpRequest request = MockHttpRequest
      .get(CHANGESET_URL + "id")
      .accept(VndMediaType.CHANGESET);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertEquals(200, response.getStatus());
    log.info("Response :{}", response.getContentAsString());
    assertTrue(response.getContentAsString().contains(String.format("\"id\":\"%s\"", id)));
    assertTrue(response.getContentAsString().contains(String.format("\"name\":\"%s\"", authorName)));
    assertTrue(response.getContentAsString().contains(String.format("\"mail\":\"%s\"", authorEmail)));
    assertTrue(response.getContentAsString().contains(String.format("\"description\":\"%s\"", commit)));
  }

}
