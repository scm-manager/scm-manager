package sonia.scm.repository.spi;

import com.google.common.io.ByteSink;
import com.google.common.io.Files;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GitBundleCommandTest {

  private GitBundleCommand bundleCommand;

  @Test
  void shouldBundleRepository(@TempDir Path temp) throws IOException {
    File repoDir = mockGitContextWithRepoDir(temp);
    if (!repoDir.exists()) {
      repoDir.mkdirs();
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

  private void addFileToRepoDir(File repoDir, String filename, String content) throws IOException {
    File file = new File(repoDir, filename);
    if (!file.exists()) {
      Files.touch(file);
    }
    Files.write(content.getBytes(), file);
  }

  private File mockGitContextWithRepoDir(Path temp) {
    GitContext gitContext = mock(GitContext.class);
    bundleCommand = new GitBundleCommand(gitContext);
    File repoDir = new File(temp.toString() + "/repository");
    when(gitContext.getDirectory()).thenReturn(repoDir);
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
