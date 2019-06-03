package sonia.scm.update.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContextProvider;
import sonia.scm.repository.RepositoryLocationResolver;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(TempDirectory.class)
@ExtendWith(MockitoExtension.class)
class MoveMigrationStrategyTest {

  @Mock
  SCMContextProvider contextProvider;
  @Mock
  RepositoryLocationResolver locationResolver;

  @BeforeEach
  void mockContextProvider(@TempDirectory.TempDir Path tempDir) {
    when(contextProvider.getBaseDirectory()).thenReturn(tempDir.toFile());
  }

  @BeforeEach
  void createV1Home(@TempDirectory.TempDir Path tempDir) throws IOException {
    V1RepositoryFileSystem.createV1Home(tempDir);
  }

  @BeforeEach
  void mockLocationResolver(@TempDirectory.TempDir Path tempDir) {
    RepositoryLocationResolver.RepositoryLocationResolverInstance instanceMock = mock(RepositoryLocationResolver.RepositoryLocationResolverInstance.class);
    when(locationResolver.forClass(Path.class)).thenReturn(instanceMock);
    when(instanceMock.getLocation(anyString())).thenAnswer(invocation -> tempDir.resolve((String) invocation.getArgument(0)));
  }

  @Test
  void shouldUseStandardDirectory(@TempDirectory.TempDir Path tempDir) {
    Path target = new MoveMigrationStrategy(contextProvider, locationResolver).migrate("b4f-a9f0-49f7-ad1f-37d3aae1c55f", "some/more/directories/than/one", "git");
    assertThat(target).isEqualTo(tempDir.resolve("b4f-a9f0-49f7-ad1f-37d3aae1c55f"));
  }

  @Test
  void shouldMoveDataDirectory(@TempDirectory.TempDir Path tempDir) {
    Path target = new MoveMigrationStrategy(contextProvider, locationResolver).migrate("b4f-a9f0-49f7-ad1f-37d3aae1c55f", "some/more/directories/than/one", "git");
    assertThat(target.resolve("data")).exists();
    Path originalDataDir = tempDir
      .resolve("repositories")
      .resolve("git")
      .resolve("some");
    assertThat(originalDataDir).doesNotExist();
  }
}
