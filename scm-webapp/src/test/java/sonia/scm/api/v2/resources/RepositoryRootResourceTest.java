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

import com.github.legman.Subscribe;
import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.NotFoundException;
import sonia.scm.PageResult;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.event.ScmEventBus;
import sonia.scm.importexport.ExportFileExtensionResolver;
import sonia.scm.importexport.ExportNotificationHandler;
import sonia.scm.importexport.ExportService;
import sonia.scm.importexport.ExportStatus;
import sonia.scm.importexport.FromBundleImporter;
import sonia.scm.importexport.FromUrlImporter;
import sonia.scm.importexport.FullScmRepositoryExporter;
import sonia.scm.importexport.FullScmRepositoryImporter;
import sonia.scm.importexport.RepositoryImportExportEncryption;
import sonia.scm.importexport.RepositoryImportLoggerFactory;
import sonia.scm.repository.CustomNamespaceStrategy;
import sonia.scm.repository.HealthCheckService;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.NamespaceStrategy;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHandler;
import sonia.scm.repository.RepositoryInitializer;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryType;
import sonia.scm.repository.api.BundleCommandBuilder;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.search.ReindexRepositoryEvent;
import sonia.scm.search.SearchEngine;
import sonia.scm.user.User;
import sonia.scm.web.RestDispatcher;
import sonia.scm.web.VndMediaType;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.Collections.singletonList;
import static java.util.stream.Stream.of;
import static javax.servlet.http.HttpServletResponse.SC_ACCEPTED;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_CONFLICT;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SubjectAware(
  username = "trillian",
  password = "secret",
  configuration = "classpath:sonia/scm/repository/shiro.ini"
)
@SuppressWarnings("UnstableApiUsage")
@RunWith(MockitoJUnitRunner.class)
public class RepositoryRootResourceTest extends RepositoryTestBase {

  private static final String REALM = "AdminRealm";

  private final RestDispatcher dispatcher = new RestDispatcher();

  @Rule
  public ShiroRule shiro = new ShiroRule();

  @Mock
  private RepositoryManager repositoryManager;
  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private RepositoryService service;
  @Mock
  private RepositoryHandler repositoryHandler;
  @Mock
  private ScmPathInfoStore scmPathInfoStore;
  @Mock
  private ScmPathInfo uriInfo;
  @Mock
  private RepositoryInitializer repositoryInitializer;
  @Mock
  private ScmConfiguration configuration;
  @Mock
  private Set<NamespaceStrategy> strategies;
  @Mock
  private FullScmRepositoryExporter fullScmRepositoryExporter;
  @Mock
  private RepositoryExportInformationToDtoMapper exportInformationToDtoMapper;
  @Mock
  private FullScmRepositoryImporter fullScmRepositoryImporter;
  @Mock
  private RepositoryImportExportEncryption repositoryImportExportEncryption;
  @Mock
  private FromUrlImporter fromUrlImporter;
  @Mock
  private FromBundleImporter fromBundleImporter;
  @Mock
  private ExportFileExtensionResolver fileExtensionResolver;
  @Mock
  private RepositoryImportLoggerFactory importLoggerFactory;
  @Mock
  private ExportService exportService;
  @Mock
  private HealthCheckService healthCheckService;
  @Mock
  private ExportNotificationHandler notificationHandler;
  @Mock
  private SearchEngine searchEngine;

  @Captor
  private ArgumentCaptor<Predicate<Repository>> filterCaptor;

  private final URI baseUri = URI.create("/");
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @InjectMocks
  private RepositoryToRepositoryDtoMapperImpl repositoryToDtoMapper;
  @InjectMocks
  private RepositoryDtoToRepositoryMapperImpl dtoToRepositoryMapper;

  private final MockHttpResponse response = new MockHttpResponse();

  @Before
  public void prepareEnvironment() throws IOException {
    super.repositoryToDtoMapper = repositoryToDtoMapper;
    super.dtoToRepositoryMapper = dtoToRepositoryMapper;
    super.manager = repositoryManager;
    RepositoryCollectionToDtoMapper repositoryCollectionToDtoMapper = new RepositoryCollectionToDtoMapper(repositoryToDtoMapper, resourceLinks);
    super.repositoryCollectionResource = new RepositoryCollectionResource(repositoryManager, repositoryCollectionToDtoMapper, dtoToRepositoryMapper, resourceLinks, repositoryInitializer);
    super.repositoryImportResource = new RepositoryImportResource(dtoToRepositoryMapper, resourceLinks, fullScmRepositoryImporter, new RepositoryImportDtoToRepositoryImportParametersMapperImpl(), repositoryImportExportEncryption, fromUrlImporter, fromBundleImporter, importLoggerFactory);
    super.repositoryExportResource = new RepositoryExportResource(repositoryManager, serviceFactory, fullScmRepositoryExporter, repositoryImportExportEncryption, exportService, exportInformationToDtoMapper, fileExtensionResolver, resourceLinks, new SimpleMeterRegistry(), notificationHandler);
    dispatcher.addSingletonResource(getRepositoryRootResource());
    when(serviceFactory.create(any(Repository.class))).thenReturn(service);
    doReturn(ImmutableSet.of(new CustomNamespaceStrategy()).iterator()).when(strategies).iterator();
    SimplePrincipalCollection trillian = new SimplePrincipalCollection("trillian", REALM);
    trillian.add(new User("trillian"), REALM);
    shiro.setSubject(
      new Subject.Builder()
        .principals(trillian)
        .authenticated(true)
        .buildSubject());
    when(repositoryImportExportEncryption.optionallyEncrypt(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));
  }

  @Test
  public void shouldFailForNotExistingRepository() throws URISyntaxException {
    when(repositoryManager.get(any(NamespaceAndName.class))).thenReturn(null);
    createRepository("space", "repo");

    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/other");

    dispatcher.invoke(request, response);

    assertEquals(SC_NOT_FOUND, response.getStatus());
  }

  @Test
  public void shouldFindExistingRepository() throws URISyntaxException, UnsupportedEncodingException {
    createRepository("space", "repo");
    when(configuration.getNamespaceStrategy()).thenReturn("CustomNamespaceStrategy");

    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo");

    dispatcher.invoke(request, response);

    assertEquals(SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("\"name\":\"repo\""));
  }

  @Test
  public void shouldGetAll() throws URISyntaxException, UnsupportedEncodingException {
    PageResult<Repository> singletonPageResult = createSingletonPageResult(createRepository("space", "repo"));
    when(repositoryManager.getPage(any(), any(), eq(0), eq(10))).thenReturn(singletonPageResult);
    when(configuration.getNamespaceStrategy()).thenReturn("CustomNamespaceStrategy");

    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2);

    dispatcher.invoke(request, response);

    assertEquals(SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("\"name\":\"repo\""));
  }

  @Test
  public void shouldCreateFilterForSearch() throws URISyntaxException {
    PageResult<Repository> singletonPageResult = createSingletonPageResult(createRepository("space", "repo"));
    when(repositoryManager.getPage(filterCaptor.capture(), any(), eq(0), eq(10))).thenReturn(singletonPageResult);
    when(configuration.getNamespaceStrategy()).thenReturn("CustomNamespaceStrategy");

    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "?q=Rep");

    dispatcher.invoke(request, response);

    assertEquals(SC_OK, response.getStatus());
    assertTrue(filterCaptor.getValue().test(new Repository("x", "git", "all_repos", "x")));
    assertTrue(filterCaptor.getValue().test(new Repository("x", "git", "x", "repository")));
    assertFalse(filterCaptor.getValue().test(new Repository("rep", "rep", "x", "x")));
  }

  @Test
  public void shouldCreateFilterForNamespace() throws URISyntaxException {
    PageResult<Repository> singletonPageResult = createSingletonPageResult(createRepository("space", "repo"));
    when(repositoryManager.getPage(filterCaptor.capture(), any(), eq(0), eq(10))).thenReturn(singletonPageResult);
    when(configuration.getNamespaceStrategy()).thenReturn("CustomNamespaceStrategy");

    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space");

    dispatcher.invoke(request, response);

    assertEquals(SC_OK, response.getStatus());
    assertTrue(filterCaptor.getValue().test(new Repository("x", "git", "space", "repo")));
    assertFalse(filterCaptor.getValue().test(new Repository("x", "git", "spaceX", "repository")));
    assertFalse(filterCaptor.getValue().test(new Repository("x", "git", "x", "space")));
  }

  @Test
  public void shouldCreateFilterForNamespaceWithQuery() throws URISyntaxException {
    PageResult<Repository> singletonPageResult = createSingletonPageResult(createRepository("space", "repo"));
    when(repositoryManager.getPage(filterCaptor.capture(), any(), eq(0), eq(10))).thenReturn(singletonPageResult);
    when(configuration.getNamespaceStrategy()).thenReturn("CustomNamespaceStrategy");

    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space?q=Rep");

    dispatcher.invoke(request, response);

    assertEquals(SC_OK, response.getStatus());
    assertTrue(filterCaptor.getValue().test(new Repository("x", "git", "space", "repo")));
    assertFalse(filterCaptor.getValue().test(new Repository("x", "git", "space", "other")));
    assertFalse(filterCaptor.getValue().test(new Repository("x", "git", "Rep", "Repository")));
  }

  @Test
  public void shouldHandleUpdateForNotExistingRepository() throws URISyntaxException, IOException {
    URL url = Resources.getResource("sonia/scm/api/v2/repository-test-update.json");
    byte[] repository = Resources.toByteArray(url);
    when(repositoryManager.get(any(NamespaceAndName.class))).thenReturn(null);

    MockHttpRequest request = MockHttpRequest
      .put("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo")
      .contentType(VndMediaType.REPOSITORY)
      .content(repository);

    dispatcher.invoke(request, response);

    assertEquals(SC_NOT_FOUND, response.getStatus());
  }

  @Test
  public void shouldHandleUpdateForExistingRepository() throws Exception {
    createRepository("space", "repo");

    URL url = Resources.getResource("sonia/scm/api/v2/repository-test-update.json");
    byte[] repository = Resources.toByteArray(url);

    MockHttpRequest request = MockHttpRequest
      .put("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo")
      .contentType(VndMediaType.REPOSITORY)
      .content(repository);

    dispatcher.invoke(request, response);

    assertEquals(SC_NO_CONTENT, response.getStatus());
    verify(repositoryManager).modify(any());
  }

  @Test
  public void shouldHandleUpdateForConcurrentlyChangedRepository() throws Exception {
    createRepository("space", "repo", 1337);

    URL url = Resources.getResource("sonia/scm/api/v2/repository-test-update.json");
    byte[] repository = Resources.toByteArray(url);

    MockHttpRequest request = MockHttpRequest
      .put("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo")
      .contentType(VndMediaType.REPOSITORY)
      .content(repository);

    dispatcher.invoke(request, response);

    assertEquals(SC_CONFLICT, response.getStatus());
    assertThat(response.getContentAsString()).contains("space/repo");
    verify(repositoryManager, never()).modify(any());
  }

  @Test
  public void shouldHandleUpdateForExistingRepositoryForChangedNamespace() throws Exception {
    createRepository("wrong", "repo");

    URL url = Resources.getResource("sonia/scm/api/v2/repository-test-update.json");
    byte[] repository = Resources.toByteArray(url);

    MockHttpRequest request = MockHttpRequest
      .put("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "wrong/repo")
      .contentType(VndMediaType.REPOSITORY)
      .content(repository);

    dispatcher.invoke(request, response);

    assertEquals(SC_BAD_REQUEST, response.getStatus());
    verify(repositoryManager, never()).modify(any());
  }

  @Test
  public void shouldHandleDeleteForExistingRepository() throws Exception {
    createRepository("space", "repo");

    MockHttpRequest request = MockHttpRequest.delete("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo");

    dispatcher.invoke(request, response);

    assertEquals(SC_NO_CONTENT, response.getStatus());
    verify(repositoryManager).delete(any());
  }

  @Test
  public void shouldCreateNewRepositoryInCorrectNamespace() throws Exception {
    when(repositoryManager.create(any())).thenAnswer(invocation -> {
      Repository repository = (Repository) invocation.getArguments()[0];
      repository.setNamespace("otherspace");
      return repository;
    });

    URL url = Resources.getResource("sonia/scm/api/v2/repository-test-update.json");
    byte[] repositoryJson = Resources.toByteArray(url);

    MockHttpRequest request = MockHttpRequest
      .post("/" + RepositoryRootResource.REPOSITORIES_PATH_V2)
      .contentType(VndMediaType.REPOSITORY)
      .content(repositoryJson);

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
    assertEquals("/v2/repositories/otherspace/repo", response.getOutputHeaders().get("Location").get(0).toString());
    verify(repositoryManager).create(any(Repository.class));
    verify(repositoryInitializer, never()).initialize(any(Repository.class), anyMap());
  }

  @Test
  public void shouldCreateNewRepositoryAndInitialize() throws Exception {
    when(repositoryManager.create(any())).thenAnswer(invocation -> invocation.getArgument(0));

    URL url = Resources.getResource("sonia/scm/api/v2/repository-test-update.json");
    byte[] repositoryJson = Resources.toByteArray(url);

    MockHttpRequest request = MockHttpRequest
      .post("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "?initialize=true")
      .contentType(VndMediaType.REPOSITORY)
      .content(repositoryJson);

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());

    ArgumentCaptor<Repository> captor = ArgumentCaptor.forClass(Repository.class);
    verify(repositoryInitializer).initialize(captor.capture(), anyMap());

    Repository repository = captor.getValue();
    assertEquals("space", repository.getNamespace());
    assertEquals("repo", repository.getName());
  }

  @Test
  public void shouldSetCurrentUserAsOwner() throws Exception {
    ArgumentCaptor<Repository> createCaptor = ArgumentCaptor.forClass(Repository.class);
    when(repositoryManager.create(createCaptor.capture())).thenAnswer(invocation -> {
      Repository repository = (Repository) invocation.getArguments()[0];
      repository.setNamespace("otherspace");
      return repository;
    });

    URL url = Resources.getResource("sonia/scm/api/v2/repository-test-update.json");
    byte[] repositoryJson = Resources.toByteArray(url);

    MockHttpRequest request = MockHttpRequest
      .post("/" + RepositoryRootResource.REPOSITORIES_PATH_V2)
      .contentType(VndMediaType.REPOSITORY)
      .content(repositoryJson);

    dispatcher.invoke(request, response);

    assertThat(createCaptor.getValue().getPermissions())
      .hasSize(1)
      .allSatisfy(p -> {
        assertThat(p.getName()).isEqualTo("trillian");
        assertThat(p.getRole()).isEqualTo("OWNER");
      });
  }

  @Test
  public void shouldCreateArrayOfProtocolUrls() throws Exception {
    createRepository("space", "repo");
    when(service.getSupportedProtocols()).thenReturn(of(new MockScmProtocol("http", "http://"), new MockScmProtocol("ssh", "ssh://")));
    when(configuration.getNamespaceStrategy()).thenReturn("CustomNamespaceStrategy");

    MockHttpRequest request = MockHttpRequest.get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo");

    dispatcher.invoke(request, response);

    assertEquals(SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("\"protocol\":[{\"href\":\"http://\",\"name\":\"http\"},{\"href\":\"ssh://\",\"name\":\"ssh\"}]"));
  }

  @Test
  public void shouldRenameRepository() throws Exception {
    String namespace = "space";
    String name = "repo";
    Repository repository1 = createRepository(namespace, name);
    when(manager.get(new NamespaceAndName(namespace, name))).thenReturn(repository1);

    URL url = Resources.getResource("sonia/scm/api/v2/rename-repo.json");
    byte[] repository = Resources.toByteArray(url);

    MockHttpRequest request = MockHttpRequest
      .post("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/rename")
      .contentType(VndMediaType.REPOSITORY)
      .content(repository);

    dispatcher.invoke(request, response);

    assertEquals(SC_NO_CONTENT, response.getStatus());
    verify(repositoryManager).rename(repository1, "space", "x");
  }

  @Test
  public void shouldMarkRepositoryAsArchived() throws Exception {
    String namespace = "space";
    String name = "repo";
    Repository repository = createRepository(namespace, name);
    when(manager.get(new NamespaceAndName(namespace, name))).thenReturn(repository);

    MockHttpRequest request = MockHttpRequest
      .post("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/archive")
      .content(new byte[]{});

    dispatcher.invoke(request, response);

    assertEquals(SC_NO_CONTENT, response.getStatus());
    verify(repositoryManager).archive(repository);
  }

  @Test
  public void shouldRemoveArchiveMarkFromRepository() throws Exception {
    String namespace = "space";
    String name = "repo";
    Repository repository = createRepository(namespace, name);
    repository.setArchived(true);
    when(manager.get(new NamespaceAndName(namespace, name))).thenReturn(repository);

    MockHttpRequest request = MockHttpRequest
      .post("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/unarchive")
      .content(new byte[]{});

    dispatcher.invoke(request, response);

    assertEquals(SC_NO_CONTENT, response.getStatus());
    verify(repositoryManager).unarchive(repository);
  }

  @Test
  public void shouldExportRepository() throws URISyntaxException {
    String namespace = "space";
    String name = "repo";
    Repository repository = createRepository(namespace, name, "svn");
    when(manager.get(new NamespaceAndName(namespace, name))).thenReturn(repository);
    mockRepositoryHandler(ImmutableSet.of(Command.BUNDLE));

    BundleCommandBuilder bundleCommandBuilder = mock(BundleCommandBuilder.class);
    when(service.getBundleCommand()).thenReturn(bundleCommandBuilder);

    MockHttpRequest request = MockHttpRequest
      .get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/export/svn");

    dispatcher.invoke(request, response);

    assertEquals(SC_OK, response.getStatus());
    assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.getOutputHeaders().get("Content-Type").get(0).toString());
    verify(service).getBundleCommand();
  }

  @Test
  public void shouldExportRepositoryCompressed() throws URISyntaxException {
    String namespace = "space";
    String name = "repo";
    Repository repository = createRepository(namespace, name, "svn");
    when(manager.get(new NamespaceAndName(namespace, name))).thenReturn(repository);
    mockRepositoryHandler(ImmutableSet.of(Command.BUNDLE));

    BundleCommandBuilder bundleCommandBuilder = mock(BundleCommandBuilder.class);
    when(service.getBundleCommand()).thenReturn(bundleCommandBuilder);

    MockHttpRequest request = MockHttpRequest
      .get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/export/svn?compressed=true");

    dispatcher.invoke(request, response);

    assertEquals(SC_OK, response.getStatus());
    assertEquals("application/x-gzip", response.getOutputHeaders().get("Content-Type").get(0).toString());
    verify(service).getBundleCommand();
  }

  @Test
  public void shouldExportFullRepository() throws URISyntaxException {
    String namespace = "space";
    String name = "repo";
    Repository repository = createRepository(namespace, name, "svn");
    when(manager.get(new NamespaceAndName(namespace, name))).thenReturn(repository);
    mockRepositoryHandler(ImmutableSet.of(Command.BUNDLE));

    MockHttpRequest request = MockHttpRequest
      .get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/export/full");

    dispatcher.invoke(request, response);

    assertEquals(SC_OK, response.getStatus());
    assertEquals("application/x-gzip", response.getOutputHeaders().get("Content-Type").get(0).toString());
    verify(fullScmRepositoryExporter).export(eq(repository), any(OutputStream.class), any());
  }

  @Test
  public void shouldExportFullRepositoryWithPassword() throws URISyntaxException {
    String namespace = "space";
    String name = "repo";
    Repository repository = createRepository(namespace, name, "svn");
    when(manager.get(new NamespaceAndName(namespace, name))).thenReturn(repository);
    mockRepositoryHandler(ImmutableSet.of(Command.BUNDLE));

    MockHttpRequest request = MockHttpRequest
      .post("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/export/full")
      .contentType(VndMediaType.REPOSITORY_EXPORT)
      .content("{\"password\": \"hitchhiker\"}".getBytes());
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_OK, response.getStatus());
    assertEquals("application/x-gzip", response.getOutputHeaders().get("Content-Type").get(0).toString());
    verify(fullScmRepositoryExporter).export(eq(repository), any(OutputStream.class), any());
  }


  @Test
  public void shouldExportFullRepositoryAsyncWithPassword() throws URISyntaxException {
    String namespace = "space";
    String name = "repo";
    Repository repository = createRepository(namespace, name, "svn");
    when(manager.get(new NamespaceAndName(namespace, name))).thenReturn(repository);
    mockRepositoryHandler(ImmutableSet.of(Command.BUNDLE));

    MockHttpRequest request = MockHttpRequest
      .post("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/export/full")
      .contentType(VndMediaType.REPOSITORY_EXPORT)
      .content("{\"password\": \"hitchhiker\", \"async\":\"true\"}".getBytes());
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_ACCEPTED, response.getStatus());
    assertEquals("/v2/repositories/space/repo/export/download", response.getOutputHeaders().getFirst("SCM-Export-Download"));
  }

  @Test
  public void shouldReturnConflictIfRepositoryAlreadyExporting() throws URISyntaxException {
    String namespace = "space";
    String name = "repo";
    Repository repository = createRepository(namespace, name, "svn");
    when(manager.get(new NamespaceAndName(namespace, name))).thenReturn(repository);
    mockRepositoryHandler(ImmutableSet.of(Command.BUNDLE));

    when(exportService.isExporting(repository)).thenReturn(true);

    MockHttpRequest request = MockHttpRequest
      .post("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/export/full")
      .contentType(VndMediaType.REPOSITORY_EXPORT)
      .content("{\"password\": \"hitchhiker\", \"async\":\"true\"}".getBytes());
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_CONFLICT, response.getStatus());
  }

  @Test
  public void shouldDeleteRepositoryExport() throws URISyntaxException {
    String namespace = "space";
    String name = "repo";
    Repository repository = createRepository(namespace, name, "svn");
    when(manager.get(new NamespaceAndName(namespace, name))).thenReturn(repository);
    mockRepositoryHandler(ImmutableSet.of(Command.BUNDLE));

    MockHttpRequest request = MockHttpRequest
      .delete("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/export");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_NO_CONTENT, response.getStatus());
    verify(exportService).clear(repository.getId());
  }

  @Test
  public void shouldReturnNotFoundIfExportDoesNotExist() throws URISyntaxException {
    String namespace = "space";
    String name = "repo";
    Repository repository = createRepository(namespace, name, "svn");
    when(manager.get(new NamespaceAndName(namespace, name))).thenReturn(repository);
    mockRepositoryHandler(ImmutableSet.of(Command.BUNDLE));

    when(exportService.isExporting(repository)).thenReturn(false);
    doThrow(NotFoundException.class).when(exportService).checkExportIsAvailable(repository);

    MockHttpRequest request = MockHttpRequest
      .get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/export/download");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_NOT_FOUND, response.getStatus());
    verify(exportService).checkExportIsAvailable(repository);
  }

  @Test
  public void shouldReturnConflictIfExportIsStillExporting() throws URISyntaxException {
    String namespace = "space";
    String name = "repo";
    Repository repository = createRepository(namespace, name, "svn");
    when(manager.get(new NamespaceAndName(namespace, name))).thenReturn(repository);
    mockRepositoryHandler(ImmutableSet.of(Command.BUNDLE));

    when(exportService.isExporting(repository)).thenReturn(true);

    MockHttpRequest request = MockHttpRequest
      .get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/export/download");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_CONFLICT, response.getStatus());
  }

  @Test
  public void shouldDownloadRepositoryExportIfReady() throws URISyntaxException, IOException {
    String namespace = "space";
    String name = "repo";
    Repository repository = createRepository(namespace, name, "svn");
    when(manager.get(new NamespaceAndName(namespace, name))).thenReturn(repository);
    mockRepositoryHandler(ImmutableSet.of(Command.BUNDLE));

    when(exportService.isExporting(repository)).thenReturn(false);
    when(exportService.getData(repository)).thenReturn(new ByteArrayInputStream("content".getBytes()));
    when(exportService.getFileExtension(repository)).thenReturn("tar.gz.enc");

    MockHttpRequest request = MockHttpRequest
      .get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/export/download");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_OK, response.getStatus());
    verify(exportService).getData(repository);
  }

  @Test
  public void shouldReturnExportInfo() throws URISyntaxException, IOException {
    String namespace = "space";
    String name = "repo";
    Repository repository = createRepository(namespace, name, "svn");
    when(manager.get(new NamespaceAndName(namespace, name))).thenReturn(repository);
    mockRepositoryHandler(ImmutableSet.of(Command.BUNDLE));

    RepositoryExportInformationDto dto = new RepositoryExportInformationDto();
    dto.setExporterName("trillian");
    dto.setCreated(Instant.ofEpochMilli(100));
    dto.setStatus(ExportStatus.EXPORTING);
    when(exportInformationToDtoMapper.map(any(), eq(repository))).thenReturn(dto);

    MockHttpRequest request = MockHttpRequest
      .get("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/export/info");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(
      "{\"exporterName\":\"trillian\",\"created\":0.100000000,\"withMetadata"
        + "\":false,\"compressed\":false,\"encrypted\":false,\"status\":\"EXPORTING\"}",
      response.getContentAsString()
    );
    assertEquals(SC_OK, response.getStatus());
    verify(exportService).getExportInformation(repository);
  }


  @Test
  public void shouldDispatchReindexEvent() throws URISyntaxException {
    ReindexTestListener listener = new ReindexTestListener();

    ScmEventBus.getInstance().register(listener);

    Repository repository = createRepository("space", "repo");

    MockHttpRequest request = MockHttpRequest
      .post("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/reindex");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);
    assertNotNull(listener.event);
    assertEquals(repository, listener.repository);
  }

  @Test
  public void shouldThrowErrorWhenMissingPermissions() throws URISyntaxException {
    Subject subject = mock(Subject.class);
    doThrow(new AuthorizationException()).when(subject).checkPermission("repository:*:space-repo");
    shiro.setSubject(subject);

    ReindexTestListener listener = new ReindexTestListener();

    ScmEventBus.getInstance().register(listener);

    createRepository("space", "repo");

    MockHttpRequest request = MockHttpRequest
      .post("/" + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/reindex");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(response.getStatus(), 403);
    assertNull(listener.event);
  }

  private static class ReindexTestListener {

    private ReindexRepositoryEvent event;

    private Repository repository;

    @Subscribe(async = false)
    public void onEvent(ReindexRepositoryEvent event) {
        this.repository = event.getRepository();
        this.event = event;
    }
  }


  private void mockRepositoryHandler(Set<Command> cmds) {
    RepositoryHandler repositoryHandler = mock(RepositoryHandler.class);
    RepositoryType repositoryType = mock(RepositoryType.class);
    when(manager.getHandler("svn")).thenReturn(repositoryHandler);
    when(repositoryHandler.getType()).thenReturn(repositoryType);
    when(repositoryType.getSupportedCommands()).thenReturn(cmds);
  }

  private PageResult<Repository> createSingletonPageResult(Repository repository) {
    return new PageResult<>(singletonList(repository), 0);
  }

  private Repository createRepository(String namespace, String name, String type) {
    Repository repository = createRepository(namespace, name);
    repository.setType(type);
    return repository;
  }

  private Repository createRepository(String namespace, String name) {
    return createRepository(namespace, name, 0);
  }

  private Repository createRepository(String namespace, String name, long lastModified) {
    Repository repository = new Repository();
    repository.setNamespace(namespace);
    repository.setName(name);
    if (lastModified > 0) {
      repository.setLastModified(lastModified);
    }
    String id = namespace + "-" + name;
    repository.setId(id);
    when(repositoryManager.get(new NamespaceAndName(namespace, name))).thenReturn(repository);
    when(repositoryManager.get(id)).thenReturn(repository);
    return repository;
  }
}
