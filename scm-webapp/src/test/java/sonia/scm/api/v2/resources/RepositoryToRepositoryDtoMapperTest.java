package sonia.scm.api.v2.resources;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.util.ThreadState;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import sonia.scm.repository.HealthCheckFailure;
import sonia.scm.repository.Permission;
import sonia.scm.repository.PermissionType;
import sonia.scm.repository.Repository;

import java.net.URI;
import java.net.URISyntaxException;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class RepositoryToRepositoryDtoMapperTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ResourceLinks resourceLinks;

  @InjectMocks
  private RepositoryToRepositoryDtoMapperImpl mapper;

  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);

  private URI expectedBaseUri;

  @Before
  public void init() throws URISyntaxException {
    initMocks(this);
    URI baseUri = new URI("http://example.com/base/");
    expectedBaseUri = baseUri.resolve(RepositoryRootResource.REPOSITORIES_PATH_V2 + "/");
    subjectThreadState.bind();
    ResourceLinksMock.initMock(resourceLinks, baseUri);
    ThreadContext.bind(subject);
  }

  @Test
  public void shouldMapSimpleProperties() {
    RepositoryDto dto = mapper.map(createTestRepository());
    assertEquals("testspace", dto.getNamespace());
    assertEquals("test", dto.getName());
    assertEquals("description", dto.getDescription());
    assertEquals("git", dto.getType());
    assertEquals("none@example.com", dto.getContact());
    assertEquals("1", dto.getId());
  }

  @Test
  public void shouldCreateLinksForUnprivilegedUser() {
    RepositoryDto dto = mapper.map(createTestRepository());
    assertEquals(
      "http://example.com/base/v2/groups/testspace/test",
      dto.getLinks().getLinkBy("self").get().getHref());
    assertFalse(dto.getLinks().getLinkBy("update").isPresent());
    assertFalse(dto.getLinks().getLinkBy("delete").isPresent());
  }

  @Test
  public void shouldCreateDeleteLink() {
    when(subject.isPermitted("repository:delete:1")).thenReturn(true);
    RepositoryDto dto = mapper.map(createTestRepository());
    assertEquals(
      "http://example.com/base/v2/groups/testspace/test",
      dto.getLinks().getLinkBy("delete").get().getHref());
  }

  @Test
  public void shouldCreateUpdateLink() {
    when(subject.isPermitted("repository:modify:1")).thenReturn(true);
    RepositoryDto dto = mapper.map(createTestRepository());
    assertEquals(
      "http://example.com/base/v2/groups/testspace/test",
      dto.getLinks().getLinkBy("update").get().getHref());
  }

  @Test
  public void shouldMapHealthCheck() {
    RepositoryDto dto = mapper.map(createTestRepository());
    assertEquals(1, dto.getHealthCheckFailures().size());
    assertEquals("summary", dto.getHealthCheckFailures().get(0).getSummary());
  }

  @Test
  public void shouldMapPermissions() {
    RepositoryDto dto = mapper.map(createTestRepository());
    assertEquals(1, dto.getPermissions().size());
    assertEquals("permission", dto.getPermissions().get(0).getName());
    assertEquals("READ", dto.getPermissions().get(0).getType());
  }

  @Test
  public void shouldCreateTagsLink() {
    RepositoryDto dto = mapper.map(createTestRepository());
    assertEquals(
      "http://example.com/base/v2/groups/testspace/test/tags/",
      dto.getLinks().getLinkBy("tags").get().getHref());
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
