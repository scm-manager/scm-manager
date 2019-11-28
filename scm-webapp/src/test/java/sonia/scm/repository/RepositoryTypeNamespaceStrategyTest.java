package sonia.scm.repository;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RepositoryTypeNamespaceStrategyTest {

  private final RepositoryTypeNamespaceStrategy namespaceStrategy = new RepositoryTypeNamespaceStrategy();

  @Test
  void shouldReturnTypeOfRepository() {
    Repository git = RepositoryTestData.create42Puzzle("git");
    assertThat(namespaceStrategy.createNamespace(git)).isEqualTo("git");

    Repository hg = RepositoryTestData.create42Puzzle("hg");
    assertThat(namespaceStrategy.createNamespace(hg)).isEqualTo("hg");
  }

}
