package sonia.scm.api.v2.resources;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.apache.shiro.util.ThreadContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import sonia.scm.repository.HealthCheckFailure;
import sonia.scm.repository.Permission;
import sonia.scm.repository.PermissionType;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.net.URI;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
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
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private RepositoryServiceFactory serviceFactory;

  @InjectMocks
  private RepositoryToRepositoryDtoMapperImpl mapper;

  @Before
  public void init() {
    initMocks(this);
    when(serviceFactory.create(any(Repository.class)).isSupported(any(Command.class))).thenReturn(true);
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
  public void shouldMapPropertiesProperty() {
    Repository repository = createTestRepository();
    repository.setProperty("testKey", "testValue");

    RepositoryDto dto = mapper.map(repository);

    assertEquals("testValue", dto.getProperties().get("testKey"));
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
    when(serviceFactory.create(any(Repository.class)).isSupported(Command.TAGS)).thenReturn(false);
    RepositoryDto dto = mapper.map(createTestRepository());
    assertFalse(dto.getLinks().getLinkBy("tags").isPresent());
  }

  @Test
  public void shouldNotCreateBranchesLink_ifNotSupported() {
    when(serviceFactory.create(any(Repository.class)).isSupported(Command.BRANCHES)).thenReturn(false);
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
    repository.setPermissions(singletonList(new Permission("permission", PermissionType.READ)));

    return repository;
  }
}
