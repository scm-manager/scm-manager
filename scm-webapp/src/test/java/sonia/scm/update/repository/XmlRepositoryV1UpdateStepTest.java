package sonia.scm.update.repository;

import com.google.common.io.Resources;
import com.google.inject.Injector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.xml.XmlRepositoryDAO;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.store.InMemoryConfigurationEntryStore;
import sonia.scm.store.InMemoryConfigurationEntryStoreFactory;
import sonia.scm.update.UpdateStepTestUtil;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sonia.scm.update.repository.MigrationStrategy.COPY;
import static sonia.scm.update.repository.MigrationStrategy.INLINE;
import static sonia.scm.update.repository.MigrationStrategy.MOVE;

@ExtendWith(MockitoExtension.class)
@ExtendWith(TempDirectory.class)
class XmlRepositoryV1UpdateStepTest {

  Injector injectorMock = MigrationStrategyMock.init();

  @Mock
  XmlRepositoryDAO repositoryDAO;
  @Mock
  MigrationStrategyDao migrationStrategyDao;

  ConfigurationEntryStoreFactory configurationEntryStoreFactory = new InMemoryConfigurationEntryStoreFactory(new InMemoryConfigurationEntryStore());

  @Captor
  ArgumentCaptor<Repository> storeCaptor;
  @Captor
  ArgumentCaptor<Path> locationCaptor;

  UpdateStepTestUtil testUtil;

  XmlRepositoryV1UpdateStep updateStep;

  @BeforeEach
  void createUpdateStepFromMocks(@TempDirectory.TempDir Path tempDir) {
    testUtil = new UpdateStepTestUtil(tempDir);
    updateStep = new XmlRepositoryV1UpdateStep(
      testUtil.getContextProvider(),
      repositoryDAO,
      migrationStrategyDao,
      injectorMock,
      configurationEntryStoreFactory
    );
  }

  @Nested
  class WithExistingDatabase {

    @BeforeEach
    void createV1Home(@TempDirectory.TempDir Path tempDir) throws IOException {
      V1RepositoryFileSystem.createV1Home(tempDir);
    }

    @BeforeEach
    void captureStoredRepositories() {
      lenient().doNothing().when(repositoryDAO).add(storeCaptor.capture(), locationCaptor.capture());
    }

    @BeforeEach
    void createMigrationPlan() {
      lenient().when(migrationStrategyDao.get("3b91caa5-59c3-448f-920b-769aaa56b761")).thenReturn(of(MOVE));
      lenient().when(migrationStrategyDao.get("c1597b4f-a9f0-49f7-ad1f-37d3aae1c55f")).thenReturn(of(COPY));
      lenient().when(migrationStrategyDao.get("454972da-faf9-4437-b682-dc4a4e0aa8eb")).thenReturn(of(INLINE));
    }

    @Test
    void shouldCreateNewRepositories() throws JAXBException {
      updateStep.doUpdate();
      verify(repositoryDAO, times(3)).add(any(), any());
    }

    @Test
    void shouldMapAttributes() throws JAXBException {
      updateStep.doUpdate();

      Optional<Repository> repository = findByNamespace("git");

      assertThat(repository)
        .get()
        .hasFieldOrPropertyWithValue("type", "git")
        .hasFieldOrPropertyWithValue("contact", "arthur@dent.uk")
        .hasFieldOrPropertyWithValue("description", "A simple repository without directories.");
    }

    @Test
    void shouldUseRepositoryTypeAsNamespaceForNamesWithSingleElement() throws JAXBException {
      updateStep.doUpdate();

      Optional<Repository> repository = findByNamespace("git");

      assertThat(repository)
        .get()
        .hasFieldOrPropertyWithValue("namespace", "git")
        .hasFieldOrPropertyWithValue("name", "simple");
    }

    @Test
    void shouldUseDirectoriesForNamespaceAndNameForNamesWithTwoElements() throws JAXBException {
      updateStep.doUpdate();

      Optional<Repository> repository = findByNamespace("one");

      assertThat(repository)
        .get()
        .hasFieldOrPropertyWithValue("namespace", "one")
        .hasFieldOrPropertyWithValue("name", "directory");
    }

    @Test
    void shouldUseDirectoriesForNamespaceAndConcatenatedNameForNamesWithMoreThanTwoElements() throws JAXBException {
      updateStep.doUpdate();

      Optional<Repository> repository = findByNamespace("some");

      assertThat(repository)
        .get()
        .hasFieldOrPropertyWithValue("namespace", "some")
        .hasFieldOrPropertyWithValue("name", "more_directories_than_one");
    }

    @Test
    void shouldMapPermissions() throws JAXBException {
      updateStep.doUpdate();

      Optional<Repository> repository = findByNamespace("git");

      assertThat(repository.get().getPermissions())
        .hasSize(3)
        .contains(
          new RepositoryPermission("mice", "WRITE", true),
          new RepositoryPermission("dent", "OWNER", false),
          new RepositoryPermission("trillian", "READ", false)
        );
    }

    @Test
    void shouldExtractPropertiesFromRepositories() throws JAXBException {
      updateStep.doUpdate();

      ConfigurationEntryStore<Object> store = configurationEntryStoreFactory.withType(null).withName("").build();
      assertThat(store.getAll())
        .hasSize(3);
    }

    @Test
    void shouldUseDirectoryFromStrategy(@TempDirectory.TempDir Path tempDir) throws JAXBException {
      Path targetDir = tempDir.resolve("someDir");
      MigrationStrategy.Instance strategyMock = injectorMock.getInstance(InlineMigrationStrategy.class);
      when(strategyMock.migrate("454972da-faf9-4437-b682-dc4a4e0aa8eb", "simple", "git")).thenReturn(targetDir);

      updateStep.doUpdate();

      assertThat(locationCaptor.getAllValues()).contains(targetDir);
    }

    @Test
    void shouldFailForMissingMigrationStrategy() {
      lenient().when(migrationStrategyDao.get("c1597b4f-a9f0-49f7-ad1f-37d3aae1c55f")).thenReturn(empty());
      assertThrows(IllegalStateException.class, () -> updateStep.doUpdate());
    }

    @Test
    void shouldBackupOldRepositoryDatabaseFile(@TempDirectory.TempDir Path tempDir) throws JAXBException {
      updateStep.doUpdate();

      assertThat(tempDir.resolve("config").resolve("repositories.xml")).doesNotExist();
      assertThat(tempDir.resolve("config").resolve("repositories.xml.v1.backup")).exists();
    }
  }

  @Test
  void shouldNotFailIfNoOldDatabaseExists() throws JAXBException {
    updateStep.doUpdate();
  }

  @Test
  void shouldNotFailIfFormerV2DatabaseExists(@TempDirectory.TempDir Path tempDir) throws JAXBException, IOException {
    createFormerV2RepositoriesFile(tempDir);

    updateStep.doUpdate();
  }

  @Test
  void shouldNotBackupFormerV2DatabaseFile(@TempDirectory.TempDir Path tempDir) throws JAXBException, IOException {
    createFormerV2RepositoriesFile(tempDir);

    updateStep.doUpdate();

    assertThat(tempDir.resolve("config").resolve("repositories.xml")).exists();
    assertThat(tempDir.resolve("config").resolve("repositories.xml.v1.backup")).doesNotExist();
  }

  private void createFormerV2RepositoriesFile(@TempDirectory.TempDir Path tempDir) throws IOException {
    URL url = Resources.getResource("sonia/scm/update/repository/formerV2RepositoryFile.xml");
    Path configDir = tempDir.resolve("config");
    Files.createDirectories(configDir);
    Files.copy(url.openStream(), configDir.resolve("repositories.xml"));
  }

  private Optional<Repository> findByNamespace(String namespace) {
    return storeCaptor.getAllValues().stream().filter(r -> r.getNamespace().equals(namespace)).findFirst();
  }
}
