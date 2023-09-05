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

package sonia.scm.plugin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PluginArchiveCleanerTest {
  private final PluginArchiveCleaner cleaner = new PluginArchiveCleaner();

  @Test
  void shouldDoNothingBecauseThresholdIsNotReached(@TempDir Path tmp) throws IOException {
    Files.createDirectories(tmp.resolve("2023-09-01"));
    Files.createDirectories(tmp.resolve("2023-09-02"));
    Files.createDirectories(tmp.resolve("2023-09-03"));
    Files.createDirectories(tmp.resolve("2023-09-04"));

    cleaner.cleanup(tmp);

    assertThat(tmp.resolve("2023-09-01")).exists();
    assertThat(tmp.resolve("2023-09-02")).exists();
    assertThat(tmp.resolve("2023-09-03")).exists();
    assertThat(tmp.resolve("2023-09-04")).exists();
  }

  @Test
  void shouldDeleteOldestFolder(@TempDir Path tmp) throws IOException {
    Files.createDirectories(tmp.resolve("2023-09-01"));
    Files.createDirectories(tmp.resolve("2023-09-02"));
    Files.createDirectories(tmp.resolve("2023-09-03"));
    Files.createDirectories(tmp.resolve("2023-09-04"));
    Files.createDirectories(tmp.resolve("2023-09-05"));
    Files.createDirectories(tmp.resolve("2023-09-06"));

    cleaner.cleanup(tmp);

    assertThat(tmp.resolve("2023-09-03")).exists();
    assertThat(tmp.resolve("2023-09-04")).exists();
    assertThat(tmp.resolve("2023-09-05")).exists();
    assertThat(tmp.resolve("2023-09-06")).exists();
    assertThat(tmp.resolve("2023-09-01")).doesNotExist();
    assertThat(tmp.resolve("2023-09-02")).doesNotExist();
  }

  @Test
  void shouldNotDeleteFiles(@TempDir Path tmp) throws IOException {
    Files.createFile(tmp.resolve("2023-09-01"));
    Files.createFile(tmp.resolve("2023-09-02"));
    Files.createFile(tmp.resolve("2023-09-03"));
    Files.createFile(tmp.resolve("2023-09-04"));
    Files.createFile(tmp.resolve("2023-09-05"));
    Files.createFile(tmp.resolve("2023-09-06"));

    cleaner.cleanup(tmp);

    assertThat(tmp.resolve("2023-09-01")).exists();
    assertThat(tmp.resolve("2023-09-02")).exists();
    assertThat(tmp.resolve("2023-09-03")).exists();
    assertThat(tmp.resolve("2023-09-04")).exists();
    assertThat(tmp.resolve("2023-09-05")).exists();
    assertThat(tmp.resolve("2023-09-06")).exists();
  }

  @Test
  void shouldNotDeleteNonMatchingFolder(@TempDir Path tmp) throws IOException {
    Files.createDirectories(tmp.resolve("A"));
    Files.createDirectories(tmp.resolve("B"));
    Files.createDirectories(tmp.resolve("C"));
    Files.createDirectories(tmp.resolve("D"));
    Files.createDirectories(tmp.resolve("E"));
    Files.createDirectories(tmp.resolve("F"));

    cleaner.cleanup(tmp);

    assertThat(tmp.resolve("A")).exists();
    assertThat(tmp.resolve("B")).exists();
    assertThat(tmp.resolve("C")).exists();
    assertThat(tmp.resolve("D")).exists();
    assertThat(tmp.resolve("E")).exists();
    assertThat(tmp.resolve("F")).exists();
  }

  @Test
  void shouldDeleteNothingWithEmptyFolder(@TempDir Path tmp) throws IOException {
    cleaner.cleanup(tmp);

    assertThat(tmp).exists();
    assertThat(tmp).isEmptyDirectory();
  }

  @Test
  void shouldNotFailWhenDirectoryDoesNotExists(@TempDir Path tmp) throws IOException {
    Path notExistingPath = tmp.resolve(".installed");
    cleaner.cleanup(notExistingPath);
  }
}

