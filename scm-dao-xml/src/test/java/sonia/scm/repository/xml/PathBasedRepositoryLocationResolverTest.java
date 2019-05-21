package sonia.scm.repository.xml;

import com.google.common.base.Charsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sonia.scm.SCMContextProvider;
import sonia.scm.repository.InitialRepositoryLocationResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(TempDirectory.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PathBasedRepositoryLocationResolverTest {

  private static final long CREATION_TIME = 42;

  @Mock
  private SCMContextProvider contextProvider;

  @Mock
  private InitialRepositoryLocationResolver initialRepositoryLocationResolver;

  @Mock
  private Clock clock;

  private Path basePath;

  private PathBasedRepositoryLocationResolver resolver;

  @BeforeEach
  void beforeEach(@TempDirectory.TempDir Path temp) {
    this.basePath = temp;
    when(contextProvider.getBaseDirectory()).thenReturn(temp.toFile());
    when(contextProvider.resolve(any(Path.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(initialRepositoryLocationResolver.getPath(anyString())).thenAnswer(invocation -> temp.resolve(invocation.getArgument(0).toString()));
    when(clock.millis()).thenReturn(CREATION_TIME);
    resolver = createResolver();
  }

  @Test
  void shouldCreateInitialDirectory() {
    Path path = resolver.forClass(Path.class).getLocation("newId");

    assertThat(path).isEqualTo(basePath.resolve("newId"));
    assertThat(path).isDirectory();
  }

  @Test
  void shouldPersistInitialDirectory() {
    resolver.forClass(Path.class).getLocation("newId");

    String content = getXmlFileContent();

    assertThat(content).contains("newId");
    assertThat(content).contains(basePath.resolve("newId").toString());
  }

  @Test
  void shouldPersistWithCreationDate() {
    long now = CREATION_TIME + 100;
    when(clock.millis()).thenReturn(now);

    resolver.forClass(Path.class).getLocation("newId");

    assertThat(resolver.getCreationTime()).isEqualTo(CREATION_TIME);

    String content = getXmlFileContent();
    assertThat(content).contains("creation-time=\"" + CREATION_TIME + "\"");
  }

  @Test
  void shouldUpdateWithModifiedDate() {
    long now = CREATION_TIME + 100;
    when(clock.millis()).thenReturn(now);

    resolver.forClass(Path.class).getLocation("newId");

    assertThat(resolver.getCreationTime()).isEqualTo(CREATION_TIME);
    assertThat(resolver.getLastModified()).isEqualTo(now);

    String content = getXmlFileContent();
    assertThat(content).contains("creation-time=\"" + CREATION_TIME + "\"");
    assertThat(content).contains("last-modified=\"" + now + "\"");
  }

  @Nested
  class WithExistingData {

    private PathBasedRepositoryLocationResolver resolverWithExistingData;

    @BeforeEach
    void createExistingDatabase() {
      resolver.forClass(Path.class).getLocation("existingId_1");
      resolver.forClass(Path.class).getLocation("existingId_2");
      resolverWithExistingData = createResolver();
    }

    @Test
    void shouldInitWithExistingData() {
      Map<String, Path> foundRepositories = new HashMap<>();
      resolverWithExistingData.forAllPaths(
        foundRepositories::put
      );
      assertThat(foundRepositories)
        .containsKeys("existingId_1", "existingId_2");
    }

    @Test
    void shouldRemoveFromFile() {
      resolverWithExistingData.remove("existingId_1");

      assertThat(getXmlFileContent()).doesNotContain("existingId_1");
    }

    @Test
    void shouldNotUpdateModificationDateForExistingDirectoryMapping() {
      long now = CREATION_TIME + 100;
      Path path = resolverWithExistingData.create(Path.class).getLocation("existingId_1");

      assertThat(path).isEqualTo(basePath.resolve("existingId_1"));

      String content = getXmlFileContent();
      assertThat(content).doesNotContain("last-modified=\"" + now + "\"");
    }

    @Test
    void shouldNotCreateDirectoryForExistingMapping() throws IOException {
      Files.delete(basePath.resolve("existingId_1"));

      Path path = resolverWithExistingData.create(Path.class).getLocation("existingId_1");

      assertThat(path).doesNotExist();
    }
  }

  private String getXmlFileContent() {
    Path storePath = basePath.resolve("config").resolve("repository-paths.xml");

    assertThat(storePath).isRegularFile();
    return content(storePath);
  }

  private PathBasedRepositoryLocationResolver createResolver() {
    return new PathBasedRepositoryLocationResolver(contextProvider, initialRepositoryLocationResolver, clock);
  }

  private String content(Path storePath) {
    try {
      return new String(Files.readAllBytes(storePath), Charsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
