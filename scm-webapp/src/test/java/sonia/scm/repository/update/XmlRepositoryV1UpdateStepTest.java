package sonia.scm.repository.update;

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
import sonia.scm.repository.spi.ZippedRepositoryTestBase;
import sonia.scm.repository.xml.XmlRepositoryDAO;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(TempDirectory.class)
class XmlRepositoryV1UpdateStepTest {

  @Mock
  SCMContextProvider contextProvider;
  @Mock
  XmlRepositoryDAO dao;

  @Captor
  ArgumentCaptor<Repository> storeCaptor;

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
      ZippedRepositoryTestBase.extract(tempDir.toFile(), "sonia/scm/repository/update/scm-home.v1.zip");
    }

    @BeforeEach
    void captureStoredRepositories() {
      doNothing().when(dao).add(storeCaptor.capture());
    }

    @Test
    void shouldCreateNewRepositories() throws JAXBException {
      updateStep.doUpdate();
      verify(dao, times(3)).add(any());
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
  }

  @Test
  void shouldNotFailIfNoOldDatabaseExists() throws JAXBException {
    updateStep.doUpdate();

  }

  private Optional<Repository> findByNamespace(String namespace) {
    return storeCaptor.getAllValues().stream().filter(r -> r.getNamespace().equals(namespace)).findFirst();
  }
}
