package sonia.scm.api.v2.resources;

import com.google.inject.util.Providers;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.util.ThreadState;
import org.assertj.core.util.Lists;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.ModificationsCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.web.VndMediaType;

import java.net.URI;
import java.text.MessageFormat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static sonia.scm.ContextEntry.ContextBuilder.noContext;

@Slf4j
@RunWith(MockitoJUnitRunner.Silent.class)
public class ModificationsResourceTest extends RepositoryTestBase {


  public static final String MODIFICATIONS_PATH = "space/repo/modifications/";
  public static final String MODIFICATIONS_URL = "/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + MODIFICATIONS_PATH;

  private Dispatcher dispatcher;

  private final URI baseUri = URI.create("/");
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @Mock
  private RepositoryServiceFactory serviceFactory;

  @Mock
  private RepositoryService repositoryService;

  @Mock
  private ModificationsCommandBuilder modificationsCommandBuilder;

  @InjectMocks
  private ModificationsToDtoMapperImpl modificationsToDtoMapper;

  private ModificationsRootResource modificationsRootResource;


  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);


  @Before
  public void prepareEnvironment() {
    modificationsRootResource = new ModificationsRootResource(serviceFactory, modificationsToDtoMapper);
    super.modificationsRootResource = Providers.of(modificationsRootResource);
    dispatcher = DispatcherMock.createDispatcher(getRepositoryRootResource());
    when(serviceFactory.create(new NamespaceAndName("space", "repo"))).thenReturn(repositoryService);
    when(serviceFactory.create(any(Repository.class))).thenReturn(repositoryService);
    when(repositoryService.getRepository()).thenReturn(new Repository("repoId", "git", "space", "repo"));
    when(repositoryService.getModificationsCommand()).thenReturn(modificationsCommandBuilder);
    subjectThreadState.bind();
    ThreadContext.bind(subject);
    when(subject.isPermitted(any(String.class))).thenReturn(true);
  }

  @After
  public void cleanupContext() {
    ThreadContext.unbindSubject();
  }

  @Test
  public void shouldGet404OnMissingModifications() throws Exception {
    when(modificationsCommandBuilder.revision(any())).thenReturn(modificationsCommandBuilder);
    when(modificationsCommandBuilder.getModifications()).thenReturn(null);

    MockHttpRequest request = MockHttpRequest
      .get(MODIFICATIONS_URL + "not_existing_revision")
      .accept(VndMediaType.MODIFICATIONS);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertEquals(404, response.getStatus());
  }

  @Test
  public void shouldGet500OnModificationsCommandError() throws Exception {
    when(modificationsCommandBuilder.revision(any())).thenReturn(modificationsCommandBuilder);
    when(modificationsCommandBuilder.getModifications()).thenThrow(new InternalRepositoryException(noContext(), "", new RuntimeException()));

    MockHttpRequest request = MockHttpRequest
      .get(MODIFICATIONS_URL + "revision")
      .accept(VndMediaType.MODIFICATIONS);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertEquals(500, response.getStatus());
  }


  @Test
  public void shouldGetModifications() throws Exception {
    Modifications modifications = new Modifications();
    String revision = "revision";
    String addedFile_1 = "a.txt";
    String addedFile_2 = "b.txt";
    String modifiedFile_1 = "d.txt";
    String modifiedFile_2 = "c.txt";
    String removedFile_1 = "e.txt";
    String removedFile_2 = "f.txt";
    modifications.setRevision(revision);
    modifications.setAdded(Lists.newArrayList(addedFile_1, addedFile_2));
    modifications.setModified(Lists.newArrayList(modifiedFile_1, modifiedFile_2));
    modifications.setRemoved(Lists.newArrayList(removedFile_1, removedFile_2));
    when(modificationsCommandBuilder.getModifications()).thenReturn(modifications);
    when(modificationsCommandBuilder.revision(eq(revision))).thenReturn(modificationsCommandBuilder);

    MockHttpRequest request = MockHttpRequest
      .get(MODIFICATIONS_URL + revision)
      .accept(VndMediaType.MODIFICATIONS);
    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    assertEquals(200, response.getStatus());
    log.info("the content: ", response.getContentAsString());
    assertTrue(response.getContentAsString().contains(String.format("\"revision\":\"%s\"", revision)));
    assertTrue(response.getContentAsString().contains(MessageFormat.format("\"added\":[\"{0}\",\"{1}\"]", addedFile_1, addedFile_2)));
    assertTrue(response.getContentAsString().contains(MessageFormat.format("\"modified\":[\"{0}\",\"{1}\"]", modifiedFile_1, modifiedFile_2)));
    assertTrue(response.getContentAsString().contains(MessageFormat.format("\"removed\":[\"{0}\",\"{1}\"]", removedFile_1, removedFile_2)));
  }
}
