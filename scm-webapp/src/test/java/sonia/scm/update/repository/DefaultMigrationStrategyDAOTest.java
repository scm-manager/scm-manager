package sonia.scm.update.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContextProvider;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.store.JAXBConfigurationStoreFactory;

import java.nio.file.Path;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static sonia.scm.update.repository.MigrationStrategy.INLINE;

@ExtendWith(MockitoExtension.class)
@ExtendWith(TempDirectory.class)
class DefaultMigrationStrategyDAOTest {

  @Mock
  SCMContextProvider contextProvider;

  private ConfigurationStoreFactory storeFactory;

  @BeforeEach
  void initStore(@TempDirectory.TempDir Path tempDir) {
    when(contextProvider.getBaseDirectory()).thenReturn(tempDir.toFile());
    storeFactory = new JAXBConfigurationStoreFactory(contextProvider, null);
  }

  @Test
  void shouldReturnEmptyOptionalWhenStoreIsEmpty() {
    DefaultMigrationStrategyDAO dao = new DefaultMigrationStrategyDAO(storeFactory);

    Optional<RepositoryMigrationPlan.RepositoryMigrationEntry> entry = dao.get("any");

    Assertions.assertThat(entry).isEmpty();
  }

  @Test
  void shouldReturnNewValue() {
    DefaultMigrationStrategyDAO dao = new DefaultMigrationStrategyDAO(storeFactory);

    dao.set("id", "git", "originalName", INLINE, "space", "name");

    Optional<RepositoryMigrationPlan.RepositoryMigrationEntry> entry = dao.get("id");

    Assertions.assertThat(entry)
      .map(RepositoryMigrationPlan.RepositoryMigrationEntry::getDataMigrationStrategy)
      .contains(INLINE);
    Assertions.assertThat(entry)
      .map(RepositoryMigrationPlan.RepositoryMigrationEntry::getNewNamespace)
      .contains("space");
    Assertions.assertThat(entry)
      .map(RepositoryMigrationPlan.RepositoryMigrationEntry::getNewName)
      .contains("name");
  }

  @Nested
  class WithExistingDatabase {
    @BeforeEach
    void initExistingDatabase() {
      DefaultMigrationStrategyDAO dao = new DefaultMigrationStrategyDAO(storeFactory);

      dao.set("id", "git", "originalName", INLINE, "space", "name");
    }

    @Test
    void shouldFindExistingValue() {
      DefaultMigrationStrategyDAO dao = new DefaultMigrationStrategyDAO(storeFactory);

      Optional<RepositoryMigrationPlan.RepositoryMigrationEntry> entry = dao.get("id");

      Assertions.assertThat(entry)
        .map(RepositoryMigrationPlan.RepositoryMigrationEntry::getDataMigrationStrategy)
        .contains(INLINE);
      Assertions.assertThat(entry)
        .map(RepositoryMigrationPlan.RepositoryMigrationEntry::getNewNamespace)
        .contains("space");
      Assertions.assertThat(entry)
        .map(RepositoryMigrationPlan.RepositoryMigrationEntry::getNewName)
        .contains("name");
      Assertions.assertThat(entry)
        .map(RepositoryMigrationPlan.RepositoryMigrationEntry::getOriginalName)
        .contains("originalName");
    }
  }
}
