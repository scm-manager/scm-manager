package sonia.scm.repository.spi;

import com.google.common.io.ByteSink;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GitBundleCommandTest {

  private GitBundleCommand bundleCommand;

  @Test
  void shouldBundleRepository(@TempDir Path temp) throws IOException {
    Path repoDir = mockGitContextWithRepoDir(temp);
    if (!Files.exists(repoDir)) {
      Files.createDirectories(repoDir);
    }
    String content = "readme testdata";
    addFileToRepoDir(repoDir, "README.md", content);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    BundleCommandRequest bundleCommandRequest = createBundleCommandRequest(baos);
    bundleCommand.bundle(bundleCommandRequest);

    assertStreamContainsContent(baos, content);
  }

  @Test
  void shouldReturnTarForGitBundleCommand() {
    bundleCommand = new GitBundleCommand(mock(GitContext.class));
    String fileExtension = bundleCommand.getFileExtension();
    assertThat(fileExtension).isEqualTo("tar");
  }

  private void addFileToRepoDir(Path repoDir, String filename, String content) throws IOException {
    Path file = repoDir.resolve(filename);
    if (!Files.exists(file)) {
      Files.createFile(file);
    }
    Files.copy(new ByteArrayInputStream(content.getBytes()), file, StandardCopyOption.REPLACE_EXISTING);
  }

  private Path mockGitContextWithRepoDir(Path temp) {
    GitContext gitContext = mock(GitContext.class);
    bundleCommand = new GitBundleCommand(gitContext);
    Path repoDir = Paths.get(temp.toString(), "repository");
    when(gitContext.getDirectory()).thenReturn(repoDir.toFile());
    return repoDir;
  }

  private BundleCommandRequest createBundleCommandRequest(ByteArrayOutputStream baos) {
    ByteSink byteSink = new ByteSink() {
      @Override
      public OutputStream openStream() {
        return baos;
      }
    };
    return new BundleCommandRequest(byteSink);
  }

  private void assertStreamContainsContent(ByteArrayOutputStream baos, String content) throws IOException {
    TarArchiveInputStream tais = new TarArchiveInputStream(new BufferedInputStream(new ByteArrayInputStream(baos.toByteArray())));
    tais.getNextEntry();

    byte[] result = new byte[(int) tais.getCurrentEntry().getSize()];
    tais.read(result);
    assertThat(new String(result)).isEqualTo(content);
  }
}
