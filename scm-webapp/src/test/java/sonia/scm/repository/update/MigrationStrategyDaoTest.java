package sonia.scm.repository.update;

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

import javax.xml.bind.JAXBException;
import java.nio.file.Path;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static sonia.scm.repository.update.MigrationStrategy.INLINE;

@ExtendWith(MockitoExtension.class)
@ExtendWith(TempDirectory.class)
class MigrationStrategyDaoTest {

  @Mock
  SCMContextProvider contextProvider;

  private ConfigurationStoreFactory storeFactory;

  @BeforeEach
  void initStore(@TempDirectory.TempDir Path tempDir) {
    when(contextProvider.getBaseDirectory()).thenReturn(tempDir.toFile());
    storeFactory = new JAXBConfigurationStoreFactory(contextProvider, null);
  }

  @Test
  void shouldReturnEmptyOptionalWhenStoreIsEmpty() throws JAXBException {
    MigrationStrategyDao dao = new MigrationStrategyDao(storeFactory);

    Optional<MigrationStrategy> strategy = dao.get("any");

    Assertions.assertThat(strategy).isEmpty();
  }

  @Test
  void shouldReturnNewValue() throws JAXBException {
    MigrationStrategyDao dao = new MigrationStrategyDao(storeFactory);

    dao.set("id", INLINE);

    Optional<MigrationStrategy> strategy = dao.get("id");

    Assertions.assertThat(strategy).contains(INLINE);
  }

  @Nested
  class WithExistingDatabase {
    @BeforeEach
    void initExistingDatabase() throws JAXBException {
      MigrationStrategyDao dao = new MigrationStrategyDao(storeFactory);

      dao.set("id", INLINE);
    }

    @Test
    void shouldFindExistingValue() throws JAXBException {
      MigrationStrategyDao dao = new MigrationStrategyDao(storeFactory);

      Optional<MigrationStrategy> strategy = dao.get("id");

      Assertions.assertThat(strategy).contains(INLINE);
    }
  }
}
