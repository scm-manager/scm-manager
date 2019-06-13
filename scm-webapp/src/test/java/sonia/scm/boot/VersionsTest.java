package sonia.scm.boot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContextProvider;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith({MockitoExtension.class, TempDirectory.class})
class VersionsTest {

  @Mock
  private SCMContextProvider contextProvider;

  @InjectMocks
  private Versions versions;

  @Test
  void shouldReturnTrueForVersionsPreviousTo160(@TempDirectory.TempDir Path directory) throws IOException {
    setVersion(directory, "1.59");
    assertThat(versions.isPreviousVersionToOld()).isTrue();

    setVersion(directory, "1.12");
    assertThat(versions.isPreviousVersionToOld()).isTrue();
  }

  @Test
  void shouldReturnFalseForVersion160(@TempDirectory.TempDir Path directory) throws IOException {
    setVersion(directory, "1.60");
    assertThat(versions.isPreviousVersionToOld()).isFalse();
  }

  @Test
  void shouldNotFailIfVersionContainsLineBreak(@TempDirectory.TempDir Path directory) throws IOException {
    setVersion(directory, "1.59\n");
    assertThat(versions.isPreviousVersionToOld()).isTrue();
  }

  @Test
  void shouldReturnFalseForVersionsNewerAs160(@TempDirectory.TempDir Path directory) throws IOException {
    setVersion(directory, "1.61");
    assertThat(versions.isPreviousVersionToOld()).isFalse();

    setVersion(directory, "1.82");
    assertThat(versions.isPreviousVersionToOld()).isFalse();
  }

  @Test
  void shouldReturnFalseForNonExistingVersionFile(@TempDirectory.TempDir Path directory) {
    setVersionFile(directory.resolve("version.txt"));
    assertThat(versions.isPreviousVersionToOld()).isFalse();
  }

  @Test
  void shouldWriteNewVersion(@TempDirectory.TempDir Path directory) {
    Path config = directory.resolve("config");
    doReturn(config).when(contextProvider).resolve(Paths.get("config"));
    doReturn("2.0.0").when(contextProvider).getVersion();

    versions.writeNewVersion();

    Path versionFile = config.resolve("version.txt");
    assertThat(versionFile).exists().hasContent("2.0.0");
  }

  private void setVersion(Path directory, String version) throws IOException {
    Path file = directory.resolve("version.txt");
    Files.write(file, version.getBytes(StandardCharsets.UTF_8));
    setVersionFile(file);
  }

  private void setVersionFile(Path file) {
    doReturn(file).when(contextProvider).resolve(Paths.get("config", "version.txt"));
  }
}
