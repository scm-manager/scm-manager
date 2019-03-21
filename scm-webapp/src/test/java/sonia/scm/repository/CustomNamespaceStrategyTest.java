package sonia.scm.repository;

import org.junit.jupiter.api.Test;
import sonia.scm.ScmConstraintViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomNamespaceStrategyTest {

  private final NamespaceStrategy namespaceStrategy = new CustomNamespaceStrategy();

  @Test
  void shouldReturnNamespaceFromRepository() {
    Repository heartOfGold = RepositoryTestData.createHeartOfGold();
    assertThat(namespaceStrategy.createNamespace(heartOfGold)).isEqualTo(RepositoryTestData.NAMESPACE);
  }

  @Test
  void shouldThrowAnValidationExceptionForAnInvalidNamespace() {
    Repository repository = new Repository();
    repository.setNamespace("..");
    repository.setName(".");

    assertThrows(ScmConstraintViolationException.class, () -> namespaceStrategy.createNamespace(repository));
  }

}
