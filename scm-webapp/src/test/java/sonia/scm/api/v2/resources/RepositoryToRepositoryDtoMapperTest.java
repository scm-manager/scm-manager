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

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.common.collect.ImmutableSet;
import org.apache.shiro.util.ThreadContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.CustomNamespaceStrategy;
import sonia.scm.repository.HealthCheckFailure;
import sonia.scm.repository.NamespaceStrategy;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.ScmProtocol;

import java.net.URI;
import java.util.Set;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.stream.Stream.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@SubjectAware(
  username = "trillian",
  password = "secret",
  configuration = "classpath:sonia/scm/repository/shiro.ini"
)
public class RepositoryToRepositoryDtoMapperTest {

  @Rule
  public final ShiroRule rule = new ShiroRule();

  private final URI baseUri = URI.create("http://example.com/base/");
  @SuppressWarnings("unused") // Is injected
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);
  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private RepositoryService repositoryService;
  @Mock
  private ScmPathInfoStore scmPathInfoStore;
  @Mock
  private ScmPathInfo uriInfo;
  @Mock
  private ScmConfiguration configuration;
  @Mock
  private Set<NamespaceStrategy> strategies;

  @InjectMocks
  private RepositoryToRepositoryDtoMapperImpl mapper;

  @Before
  public void init() {
    initMocks(this);
    when(serviceFactory.create(any(Repository.class))).thenReturn(repositoryService);
    when(repositoryService.isSupported(any(Command.class))).thenReturn(true);
    when(repositoryService.getSupportedProtocols()).thenReturn(of());
    when(scmPathInfoStore.get()).thenReturn(uriInfo);
    when(configuration.getNamespaceStrategy()).thenReturn("CustomNamespaceStrategy");
    when(uriInfo.getApiRestUri()).thenReturn(URI.create("/x/y"));
    doReturn(ImmutableSet.of(new CustomNamespaceStrategy()).iterator()).when(strategies).iterator();
  }

  @After
  public void cleanup() {
    ThreadContext.unbindSubject();
  }

  @Test
  public void shouldMapSimpleProperties() {
    RepositoryDto dto = mapper.map(createTestRepository());
    assertEquals("testspace", dto.getNamespace());
    assertEquals("test", dto.getName());
    assertEquals("description", dto.getDescription());
    assertEquals("git", dto.getType());
    assertEquals("none@example.com", dto.getContact());
  }

  @Test
  @SubjectAware(username = "unpriv")
  public void shouldCreateLinksForUnprivilegedUser() {
    RepositoryDto dto = mapper.map(createTestRepository());
    assertEquals(
      "http://example.com/base/v2/repositories/testspace/test",
      dto.getLinks().getLinkBy("self").get().getHref());
    assertFalse(dto.getLinks().getLinkBy("update").isPresent());
    assertFalse(dto.getLinks().getLinkBy("delete").isPresent());
    assertFalse(dto.getLinks().getLinkBy("permissions").isPresent());
  }

  @Test
  public void shouldCreateDeleteLink() {
    RepositoryDto dto = mapper.map(createTestRepository());
    assertEquals(
      "http://example.com/base/v2/repositories/testspace/test",
      dto.getLinks().getLinkBy("delete").get().getHref());
  }

  @Test
  public void shouldCreateUpdateLink() {
    RepositoryDto dto = mapper.map(createTestRepository());
    assertEquals(
      "http://example.com/base/v2/repositories/testspace/test",
      dto.getLinks().getLinkBy("update").get().getHref());
  }

  @Test
  public void shouldCreateRenameLink() {
    when(configuration.getNamespaceStrategy()).thenReturn("test");
    RepositoryDto dto = mapper.map(createTestRepository());
    assertEquals(
      "http://example.com/base/v2/repositories/testspace/test/rename",
      dto.getLinks().getLinkBy("rename").get().getHref());
  }

  @Test
  public void shouldCreateRenameWithNamespaceLink() {
    RepositoryDto dto = mapper.map(createTestRepository());
    assertEquals(
      "http://example.com/base/v2/repositories/testspace/test/rename",
      dto.getLinks().getLinkBy("renameWithNamespace").get().getHref());
  }

  @Test
  public void shouldMapHealthCheck() {
    RepositoryDto dto = mapper.map(createTestRepository());
    assertEquals(1, dto.getHealthCheckFailures().size());
    assertEquals("summary", dto.getHealthCheckFailures().get(0).getSummary());
  }

  @Test
  public void shouldCreateTagsLink_ifSupported() {
    RepositoryDto dto = mapper.map(createTestRepository());
    assertEquals(
      "http://example.com/base/v2/repositories/testspace/test/tags/",
      dto.getLinks().getLinkBy("tags").get().getHref());
  }

  @Test
  public void shouldCreateBranchesLink_ifSupported() {
    RepositoryDto dto = mapper.map(createTestRepository());
    assertEquals(
      "http://example.com/base/v2/repositories/testspace/test/branches/",
      dto.getLinks().getLinkBy("branches").get().getHref());
  }

  @Test
  public void shouldNotCreateTagsLink_ifNotSupported() {
    when(repositoryService.isSupported(Command.TAGS)).thenReturn(false);
    RepositoryDto dto = mapper.map(createTestRepository());
    assertFalse(dto.getLinks().getLinkBy("tags").isPresent());
  }

  @Test
  public void shouldNotCreateBranchesLink_ifNotSupported() {
    when(repositoryService.isSupported(Command.BRANCHES)).thenReturn(false);
    RepositoryDto dto = mapper.map(createTestRepository());
    assertFalse(dto.getLinks().getLinkBy("branches").isPresent());
  }

  @Test
  public void shouldCreateChangesetsLink() {
    RepositoryDto dto = mapper.map(createTestRepository());
    assertEquals(
      "http://example.com/base/v2/repositories/testspace/test/changesets/",
      dto.getLinks().getLinkBy("changesets").get().getHref());
  }

  @Test
  public void shouldCreateSourcesLink() {
    RepositoryDto dto = mapper.map(createTestRepository());
    assertEquals(
      "http://example.com/base/v2/repositories/testspace/test/sources/",
      dto.getLinks().getLinkBy("sources").get().getHref());
  }

  @Test
  public void shouldCreatePermissionsLink() {
    RepositoryDto dto = mapper.map(createTestRepository());
    assertEquals(
      "http://example.com/base/v2/repositories/testspace/test/permissions/",
      dto.getLinks().getLinkBy("permissions").get().getHref());
  }

  @Test
  public void shouldCreateCorrectProtocolLinks() {
    when(repositoryService.getSupportedProtocols()).thenReturn(
      of(mockProtocol("http", "http://scm"), mockProtocol("other", "some://protocol"))
    );

    RepositoryDto dto = mapper.map(createTestRepository());
    assertTrue("should contain http link", dto.getLinks().stream().anyMatch(l -> l.getName().equals("http") && l.getHref().equals("http://scm")));
    assertTrue("should contain other link", dto.getLinks().stream().anyMatch(l -> l.getName().equals("other") && l.getHref().equals("some://protocol")));
  }

  @Test
  @SubjectAware(username = "community")
  public void shouldCreateProtocolLinksForPullPermission() {
    when(repositoryService.getSupportedProtocols()).thenReturn(
      of(mockProtocol("http", "http://scm"), mockProtocol("other", "some://protocol"))
    );

    RepositoryDto dto = mapper.map(createTestRepository());
    assertEquals(2, dto.getLinks().getLinksBy("protocol").size());
  }

  @Test
  @SubjectAware(username = "unpriv")
  public void shouldNotCreateProtocolLinksWithoutPullPermission() {
    when(repositoryService.getSupportedProtocols()).thenReturn(
      of(mockProtocol("http", "http://scm"), mockProtocol("other", "some://protocol"))
    );

    RepositoryDto dto = mapper.map(createTestRepository());
    assertTrue(dto.getLinks().getLinksBy("protocol").isEmpty());
  }

  @Test
  public void shouldAppendLinks() {
    HalEnricherRegistry registry = new HalEnricherRegistry();
    registry.register(Repository.class, (ctx, appender) -> {
      Repository repository = ctx.oneRequireByType(Repository.class);
      appender.appendLink("id", "http://" + repository.getId());
    });
    mapper.setRegistry(registry);

    RepositoryDto dto = mapper.map(createTestRepository());
    assertEquals("http://1", dto.getLinks().getLinkBy("id").get().getHref());
  }

  @Test
  public void shouldCreateArchiveLink() {
    RepositoryDto dto = mapper.map(createTestRepository());
    assertEquals(
      "http://example.com/base/v2/repositories/testspace/test/archive",
      dto.getLinks().getLinkBy("archive").get().getHref());
  }

  @Test
  public void shouldCreateUnArchiveLink() {
    Repository repository = createTestRepository();
    repository.setArchived(true);
    RepositoryDto dto = mapper.map(repository);
    assertEquals(
      "http://example.com/base/v2/repositories/testspace/test/unarchive",
      dto.getLinks().getLinkBy("unarchive").get().getHref());
  }

  @Test
  public void shouldCreateExportLink() {
    Repository repository = createTestRepository();
    repository.setType("svn");
    RepositoryDto dto = mapper.map(repository);
    assertEquals(
      "http://example.com/base/v2/repositories/testspace/test/export/svn",
      dto.getLinks().getLinkBy("export").get().getHref());
  }

  @Test
  public void shouldCreateFullExportLink() {
    Repository repository = createTestRepository();
    repository.setType("svn");
    RepositoryDto dto = mapper.map(repository);
    assertEquals(
      "http://example.com/base/v2/repositories/testspace/test/export/full",
      dto.getLinks().getLinkBy("fullExport").get().getHref());
  }

  private ScmProtocol mockProtocol(String type, String protocol) {
    return new MockScmProtocol(type, protocol);
  }

  private Repository createTestRepository() {
    Repository repository = new Repository();
    repository.setNamespace("testspace");
    repository.setName("test");
    repository.setDescription("description");
    repository.setType("git");
    repository.setContact("none@example.com");
    repository.setId("1");
    repository.setCreationDate(System.currentTimeMillis());
    repository.setHealthCheckFailures(singletonList(new HealthCheckFailure("1", "summary", "url", "failure")));

    return repository;
  }

}
