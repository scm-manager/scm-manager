package sonia.scm.repository;

import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class RepositoryPermissionTest {

  @Test
  void shouldBeEqualWithSameVerbs() {
    RepositoryPermission permission1 = new RepositoryPermission("name", asList("one", "two"), false);
    RepositoryPermission permission2 = new RepositoryPermission("name", asList("two", "one"), false);

    assertThat(permission1).isEqualTo(permission2);
  }

  @Test
  void shouldHaveSameHashCodeWithSameVerbs() {
    long hash1 = new RepositoryPermission("name", asList("one", "two"), false).hashCode();
    long hash2 = new RepositoryPermission("name", asList("two", "one"), false).hashCode();

    assertThat(hash1).isEqualTo(hash2);
  }

  @Test
  void shouldNotBeEqualWithSameVerbs() {
    RepositoryPermission permission1 = new RepositoryPermission("name", asList("one", "two"), false);
    RepositoryPermission permission2 = new RepositoryPermission("name", asList("three", "one"), false);

    assertThat(permission1).isNotEqualTo(permission2);
  }

  @Test
  void shouldNotBeEqualWithDifferentType() {
    RepositoryPermission permission1 = new RepositoryPermission("name", asList("one"), false);
    RepositoryPermission permission2 = new RepositoryPermission("name", asList("one"), true);

    assertThat(permission1).isNotEqualTo(permission2);
  }

  @Test
  void shouldNotBeEqualWithDifferentName() {
    RepositoryPermission permission1 = new RepositoryPermission("name1", asList("one"), false);
    RepositoryPermission permission2 = new RepositoryPermission("name2", asList("one"), false);

    assertThat(permission1).isNotEqualTo(permission2);
  }
}
