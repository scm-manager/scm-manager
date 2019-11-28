package sonia.scm.repository;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class RepositoryTest {

  @Test
  void shouldCreateNewPermissionOnClone() {
    Repository repository = new Repository();
    repository.setPermissions(Arrays.asList(new RepositoryPermission("one", "role", false)));

    Repository cloned = repository.clone();
    cloned.setPermissions(Arrays.asList(new RepositoryPermission("two", "role", false)));

    assertThat(repository.getPermissions()).extracting(r -> r.getName()).containsOnly("one");
  }

}
