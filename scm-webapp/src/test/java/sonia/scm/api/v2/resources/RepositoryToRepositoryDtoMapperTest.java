package sonia.scm.api.v2.resources;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.apache.shiro.util.ThreadContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import sonia.scm.repository.HealthCheckFailure;
import sonia.scm.repository.Permission;
import sonia.scm.repository.PermissionType;
import sonia.scm.repository.Repository;

import java.net.URI;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @InjectMocks
  private RepositoryToRepositoryDtoMapperImpl mapper;

  @Before
  public void init() {
    initMocks(this);
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
  public void shouldMapHealthCheck() {
    RepositoryDto dto = mapper.map(createTestRepository());
    assertEquals(1, dto.getHealthCheckFailures().size());
    assertEquals("summary", dto.getHealthCheckFailures().get(0).getSummary());
  }

  @Test
  public void shouldCreateTagsLink() {
    RepositoryDto dto = mapper.map(createTestRepository());
    assertEquals(
      "http://example.com/base/v2/repositories/testspace/test/tags/",
      dto.getLinks().getLinkBy("tags").get().getHref());
  }

  @Test
  public void shouldCreateBranchesLink() {
    RepositoryDto dto = mapper.map(createTestRepository());
    assertEquals(
      "http://example.com/base/v2/repositories/testspace/test/branches/",
      dto.getLinks().getLinkBy("branches").get().getHref());
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
    repository.setHealthCheckFailures(asList(new HealthCheckFailure("1", "summary", "url", "failure")));
    repository.setPermissions(asList(new Permission("permission", PermissionType.READ)));

    return repository;
  }
}
