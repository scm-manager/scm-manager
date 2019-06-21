package sonia.scm.store;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContextProvider;
import sonia.scm.io.DefaultFileSystem;
import sonia.scm.repository.InitialRepositoryLocationResolver;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.repository.xml.PathBasedRepositoryLocationResolver;
import sonia.scm.update.PropertyFileAccess;
import sonia.scm.util.IOUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith(TempDirectory.class)
@ExtendWith(MockitoExtension.class)
class JAXBPropertyFileAccessTest {

  public static final String REPOSITORY_ID = "repoId";
  public static final String STORE_NAME = "test";

  @Mock
  SCMContextProvider contextProvider;

  RepositoryLocationResolver locationResolver;

  JAXBPropertyFileAccess fileAccess;

  @BeforeEach
  void initTempDir(@TempDirectory.TempDir Path tempDir) {
    lenient().when(contextProvider.getBaseDirectory()).thenReturn(tempDir.toFile());
    lenient().when(contextProvider.resolve(any())).thenAnswer(invocation -> tempDir.resolve(invocation.getArgument(0).toString()));

    locationResolver = new PathBasedRepositoryLocationResolver(contextProvider, new InitialRepositoryLocationResolver(), new DefaultFileSystem());//new TempDirRepositoryLocationResolver(tempDir.toFile());

    fileAccess = new JAXBPropertyFileAccess(contextProvider, locationResolver);
  }

  @Nested
  class ForExistingRepository {


    @BeforeEach
    void createRepositoryLocation() {
      locationResolver.forClass(Path.class).createLocation(REPOSITORY_ID);
    }

    @Test
    void shouldMoveStoreFileToRepositoryBasedLocation(@TempDirectory.TempDir Path tempDir) throws IOException {
      createV1StoreFile(tempDir, "myStore.xml");

      fileAccess.forStoreName(STORE_NAME).moveAsRepositoryStore(Paths.get("myStore.xml"), REPOSITORY_ID);

      Assertions.assertThat(tempDir.resolve("repositories").resolve(REPOSITORY_ID).resolve("store").resolve("data").resolve(STORE_NAME).resolve("myStore.xml")).exists();
    }

    @Test
    void shouldMoveAllStoreFilesToRepositoryBasedLocations(@TempDirectory.TempDir Path tempDir) throws IOException {
      locationResolver.forClass(Path.class).createLocation("repoId2");

      createV1StoreFile(tempDir, REPOSITORY_ID + ".xml");
      createV1StoreFile(tempDir, "repoId2.xml");

      PropertyFileAccess.StoreFileTools statisticStoreAccess = fileAccess.forStoreName(STORE_NAME);
      statisticStoreAccess.forStoreFiles(statisticStoreAccess::moveAsRepositoryStore);

      Assertions.assertThat(tempDir.resolve("repositories").resolve(REPOSITORY_ID).resolve("store").resolve("data").resolve(STORE_NAME).resolve("repoId.xml")).exists();
      Assertions.assertThat(tempDir.resolve("repositories").resolve("repoId2").resolve("store").resolve("data").resolve(STORE_NAME).resolve("repoId2.xml")).exists();
    }
  }

  private void createV1StoreFile(@TempDirectory.TempDir Path tempDir, String name) throws IOException {
    Path v1Dir = tempDir.resolve("var").resolve("data").resolve(STORE_NAME);
    IOUtil.mkdirs(v1Dir.toFile());
    Files.createFile(v1Dir.resolve(name));
  }

  @Nested
  class ForMissingRepository {

    @Test
    void shouldIgnoreStoreFile(@TempDirectory.TempDir Path tempDir) throws IOException {
      createV1StoreFile(tempDir, "myStore.xml");

      fileAccess.forStoreName(STORE_NAME).moveAsRepositoryStore(Paths.get("myStore.xml"), REPOSITORY_ID);

      Assertions.assertThat(tempDir.resolve("repositories").resolve(REPOSITORY_ID).resolve("store").resolve("data").resolve(STORE_NAME).resolve("myStore.xml")).doesNotExist();
    }
  }
}
