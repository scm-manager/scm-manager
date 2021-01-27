package sonia.scm.repository.spi;

import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GitUnbundleCommandTest extends AbstractGitCommandTestBase {
  private GitContext gitContext;
  private GitUnbundleCommand unbundleCommand;

  @BeforeEach
  void initCommand() {
    gitContext = mock(GitContext.class);
    unbundleCommand = new GitUnbundleCommand(gitContext);
  }

  @Test
  void shouldUnbundleRepositoryFiles(@TempDir Path temp) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);
    String filePath = "test-input";
    String fileContent = "HeartOfGold";
    addEntry(taos, filePath, fileContent);
    taos.finish();

    when(gitContext.getDirectory()).thenReturn(temp.toFile());
    ByteSource byteSource = ByteSource.wrap(baos.toByteArray());
    UnbundleCommandRequest unbundleCommandRequest = new UnbundleCommandRequest(byteSource);

    unbundleCommand.unbundle(unbundleCommandRequest);

    File createdFile = temp.resolve(filePath).toFile();
    assertThat(createdFile).exists();
    assertThat(Files.readLines(createdFile, StandardCharsets.UTF_8).get(0)).isEqualTo(fileContent);
  }

  @Test
  void shouldUnbundleNestedRepositoryFiles(@TempDir Path temp) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);
    String filePath = "objects/pack/test-input";
    String fileContent = "hitchhiker";
    addEntry(taos, filePath, fileContent);
    taos.finish();

    when(gitContext.getDirectory()).thenReturn(temp.toFile());
    ByteSource byteSource = ByteSource.wrap(baos.toByteArray());
    UnbundleCommandRequest unbundleCommandRequest = new UnbundleCommandRequest(byteSource);

    unbundleCommand.unbundle(unbundleCommandRequest);

    File createdFile = temp.resolve(filePath).toFile();
    assertThat(createdFile).exists();
    assertThat(Files.readLines(createdFile, StandardCharsets.UTF_8).get(0)).isEqualTo(fileContent);
  }

  private void addEntry(TarArchiveOutputStream taos, String name, String input) throws IOException {
    TarArchiveEntry entry = new TarArchiveEntry(name);
    byte[] data = input.getBytes();
    entry.setSize(data.length);
    taos.putArchiveEntry(entry);
    taos.write(data);
    taos.closeArchiveEntry();
  }
}
