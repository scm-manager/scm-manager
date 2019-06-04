package sonia.scm.update.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContextProvider;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(TempDirectory.class)
@ExtendWith(MockitoExtension.class)
class InlineMigrationStrategyTest {

  @Mock
  SCMContextProvider contextProvider;

  @BeforeEach
  void mockContextProvider(@TempDirectory.TempDir Path tempDir) {
    when(contextProvider.getBaseDirectory()).thenReturn(tempDir.toFile());
  }

  @BeforeEach
  void createV1Home(@TempDirectory.TempDir Path tempDir) throws IOException {
    V1RepositoryFileSystem.createV1Home(tempDir);
  }

  @Test
  void shouldUseExistingDirectory(@TempDirectory.TempDir Path tempDir) {
    Path target = new InlineMigrationStrategy(contextProvider).migrate("b4f-a9f0-49f7-ad1f-37d3aae1c55f", "some/more/directories/than/one", "git");
    assertThat(target).isEqualTo(resolveOldDirectory(tempDir));
  }

  @Test
  void shouldMoveDataDirectory(@TempDirectory.TempDir Path tempDir) {
    new InlineMigrationStrategy(contextProvider).migrate("b4f-a9f0-49f7-ad1f-37d3aae1c55f", "some/more/directories/than/one", "git");
    assertThat(resolveOldDirectory(tempDir).resolve("data")).exists();
  }

  private Path resolveOldDirectory(Path tempDir) {
    return tempDir.resolve("repositories").resolve("git").resolve("some").resolve("more").resolve("directories").resolve("than").resolve("one");
  }
}
