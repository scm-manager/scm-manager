/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package sonia.scm.repository.spi;

import com.google.common.io.ByteSink;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
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
import static sonia.scm.util.Archives.readTarStream;

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
    TarArchiveInputStream tais = readTarStream(new BufferedInputStream(new ByteArrayInputStream(baos.toByteArray())));
    tais.getNextEntry();

    byte[] result = IOUtils.toByteArray(tais);
    assertThat(new String(result)).isEqualTo(content);
  }
}
