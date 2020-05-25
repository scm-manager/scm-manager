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

package sonia.scm.lifecycle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
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

@ExtendWith({MockitoExtension.class})
class VersionsTest {

  @Mock
  private SCMContextProvider contextProvider;

  @InjectMocks
  private Versions versions;

  @Test
  void shouldReturnTrueForVersionsPreviousTo160(@TempDir Path directory) throws IOException {
    setVersion(directory, "1.59");
    assertThat(versions.isPreviousVersionTooOld()).isTrue();

    setVersion(directory, "1.12");
    assertThat(versions.isPreviousVersionTooOld()).isTrue();
  }

  @Test
  void shouldReturnFalseForVersion160(@TempDir Path directory) throws IOException {
    setVersion(directory, "1.60");
    assertThat(versions.isPreviousVersionTooOld()).isFalse();
  }

  @Test
  void shouldNotFailIfVersionContainsLineBreak(@TempDir Path directory) throws IOException {
    setVersion(directory, "1.59\n");
    assertThat(versions.isPreviousVersionTooOld()).isTrue();
  }

  @Test
  void shouldReturnFalseForVersionsNewerAs160(@TempDir Path directory) throws IOException {
    setVersion(directory, "1.61");
    assertThat(versions.isPreviousVersionTooOld()).isFalse();

    setVersion(directory, "1.82");
    assertThat(versions.isPreviousVersionTooOld()).isFalse();
  }

  @Test
  void shouldReturnFalseForNonExistingVersionFile(@TempDir Path directory) {
    setVersionFile(directory.resolve("version.txt"));
    assertThat(versions.isPreviousVersionTooOld()).isFalse();
  }

  @Test
  void shouldWriteNewVersion(@TempDir Path directory) {
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
