/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
import static sonia.scm.util.Archives.createTarInputStream;

class HgBundleCommandTest {

  private HgBundleCommand bundleCommand;

  @Test
  void shouldBundleRepository(@TempDir Path temp) throws IOException {
    Path repoDir = mockHgContextWithRepoDir(temp);
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
    bundleCommand = new HgBundleCommand(mock(HgCommandContext.class));
     String fileExtension = bundleCommand.getFileExtension();
     assertThat(fileExtension).isEqualTo("tar");
  }

  private void addFileToRepoDir(Path repoDir, String filename, String content) throws IOException {
    Path file = repoDir.resolve(filename);
    Files.copy(new ByteArrayInputStream(content.getBytes()), file, StandardCopyOption.REPLACE_EXISTING);
  }

  private Path mockHgContextWithRepoDir(Path temp) {
    HgCommandContext context = mock(HgCommandContext.class);
    bundleCommand = new HgBundleCommand(context);
    Path repoDir = Paths.get(temp.toString(), "repository");
    when(context.getDirectory()).thenReturn(repoDir.toFile());
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
    TarArchiveInputStream tais = createTarInputStream(new BufferedInputStream(new ByteArrayInputStream(baos.toByteArray())));
    tais.getNextEntry();

    byte[] result = IOUtils.toByteArray(tais);
    assertThat(new String(result)).isEqualTo(content);
  }

}
