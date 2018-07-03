package sonia.scm.api.v2.resources;

import org.junit.Test;
import sonia.scm.repository.HealthCheckFailure;
import sonia.scm.repository.Permission;
import sonia.scm.repository.PermissionType;
import sonia.scm.repository.Repository;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class RepositoryToRepositoryDtoMapperTest {

  private RepositoryToRepositoryDtoMapperImpl mapper = new RepositoryToRepositoryDtoMapperImpl();

  @Test
  public void shouldMapSimpleProperties() {
    RepositoryDto dto = mapper.map(createDummyRepository());
    assertEquals("namespace", dto.getNamespace());
    assertEquals("name", dto.getName());
    assertEquals("description", dto.getDescription());
    assertEquals("git", dto.getType());
    assertEquals("none@example.com", dto.getContact());
    assertEquals("1", dto.getId());
  }

  @Test
  public void shouldMapHealthCheck() {
    RepositoryDto dto = mapper.map(createDummyRepository());
    assertEquals(1, dto.getHealthCheckFailures().size());
    assertEquals("summary", dto.getHealthCheckFailures().get(0).getSummary());
  }

  @Test
  public void shouldMapPermissions() {
    RepositoryDto dto = mapper.map(createDummyRepository());
    assertEquals(1, dto.getPermissions().size());
    assertEquals("permission", dto.getPermissions().get(0).getName());
    assertEquals("READ", dto.getPermissions().get(0).getType());
  }

  private Repository createDummyRepository() {
    Repository repository = new Repository();
    repository.setNamespace("namespace");
    repository.setName("name");
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
