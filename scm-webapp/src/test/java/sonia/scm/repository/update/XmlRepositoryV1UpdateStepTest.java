package sonia.scm.repository.update;

import com.google.common.io.Resources;
import com.google.inject.Injector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContextProvider;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.xml.XmlRepositoryDAO;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sonia.scm.repository.update.MigrationStrategy.COPY;
import static sonia.scm.repository.update.MigrationStrategy.INLINE;
import static sonia.scm.repository.update.MigrationStrategy.MOVE;

@ExtendWith(MockitoExtension.class)
@ExtendWith(TempDirectory.class)
class XmlRepositoryV1UpdateStepTest {

  Injector injectorMock = MigrationStrategyMock.init();

  @Mock
  SCMContextProvider contextProvider;
  @Mock
  XmlRepositoryDAO repositoryDAO;
  @Mock()
  MigrationStrategyDao migrationStrategyDao;

  @Captor
  ArgumentCaptor<Repository> storeCaptor;
  @Captor
  ArgumentCaptor<Path> locationCaptor;

  @InjectMocks
  XmlRepositoryV1UpdateStep updateStep;

  @BeforeEach
  void mockScmHome(@TempDirectory.TempDir Path tempDir) {
    when(contextProvider.getBaseDirectory()).thenReturn(tempDir.toFile());
  }

  @Nested
  class WithExistingDatabase {

    @BeforeEach
    void createV1Home(@TempDirectory.TempDir Path tempDir) throws IOException {
      V1RepositoryFileSystem.createV1Home(tempDir);
    }

    @BeforeEach
    void captureStoredRepositories() {
      doNothing().when(repositoryDAO).add(storeCaptor.capture(), locationCaptor.capture());
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
    void shouldUseDirectoryFromStrategy(@TempDirectory.TempDir Path tempDir) throws JAXBException {
      Path targetDir = tempDir.resolve("someDir");
      MigrationStrategy.Instance strategyMock = injectorMock.getInstance(InlineMigrationStrategy.class);
      when(strategyMock.migrate("454972da-faf9-4437-b682-dc4a4e0aa8eb", "simple", "git")).thenReturn(targetDir);

      updateStep.doUpdate();

      assertThat(locationCaptor.getAllValues()).contains(targetDir);
    }
  }

  @Test
  void shouldNotFailIfNoOldDatabaseExists() throws JAXBException {
    updateStep.doUpdate();
  }

  @Test
  void shouldNotFailIfFormerV1DatabaseExists(@TempDirectory.TempDir Path tempDir) throws JAXBException, IOException {
    URL url = Resources.getResource("sonia/scm/repository/update/formerV2RepositoryFile.xml");
    Path configDir = tempDir.resolve("config");
    Files.createDirectories(configDir);
    Files.copy(url.openStream(), configDir.resolve("repositories.xml"));
    updateStep.doUpdate();
  }

  private Optional<Repository> findByNamespace(String namespace) {
    return storeCaptor.getAllValues().stream().filter(r -> r.getNamespace().equals(namespace)).findFirst();
  }
}
