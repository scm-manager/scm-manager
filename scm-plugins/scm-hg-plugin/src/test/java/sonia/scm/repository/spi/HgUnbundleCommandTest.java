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

import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sonia.scm.util.Archives;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HgUnbundleCommandTest {
  private HgCommandContext hgContext;
  private HgUnbundleCommand unbundleCommand;

  @BeforeEach
  void initCommand() {
    hgContext = mock(HgCommandContext.class);
    unbundleCommand = new HgUnbundleCommand(hgContext);
  }

  @Test
  void shouldUnbundleRepositoryFiles(@TempDir Path temp) throws IOException {
    String filePath = "test-input";
    String fileContent = "HeartOfGold";
    UnbundleCommandRequest unbundleCommandRequest = createUnbundleCommandRequestForFile(temp, filePath, fileContent);

    unbundleCommand.unbundle(unbundleCommandRequest);

    assertFileWithContentWasCreated(temp, filePath, fileContent);
  }

  @Test
  void shouldUnbundleNestedRepositoryFiles(@TempDir Path temp) throws IOException {
    String filePath = "objects/pack/test-input";
    String fileContent = "hitchhiker";
    UnbundleCommandRequest unbundleCommandRequest = createUnbundleCommandRequestForFile(temp, filePath, fileContent);

    unbundleCommand.unbundle(unbundleCommandRequest);

    assertFileWithContentWasCreated(temp, filePath, fileContent);
  }

  private void assertFileWithContentWasCreated(@TempDir Path temp, String filePath, String fileContent) throws IOException {
    File createdFile = temp.resolve(filePath).toFile();
    assertThat(createdFile).exists();
    assertThat(Files.readLines(createdFile, StandardCharsets.UTF_8).get(0)).isEqualTo(fileContent);
  }

  private UnbundleCommandRequest createUnbundleCommandRequestForFile(Path temp, String filePath, String fileContent) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    TarArchiveOutputStream taos = Archives.createTarOutputStream(baos);
    addEntry(taos, filePath, fileContent);
    taos.finish();
    taos.close();

    when(hgContext.getDirectory()).thenReturn(temp.toFile());
    ByteSource byteSource = ByteSource.wrap(baos.toByteArray());
    UnbundleCommandRequest unbundleCommandRequest = new UnbundleCommandRequest(byteSource);
    return unbundleCommandRequest;
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
