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
    
package sonia.scm.store;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static sonia.scm.store.CopyOnWrite.withTemporaryFile;

@ExtendWith(TempDirectory.class)
class CopyOnWriteTest {

  @Test
  void shouldCreateNewFile(@TempDirectory.TempDir Path tempDir) {
    Path expectedFile = tempDir.resolve("toBeCreated.txt");

    withTemporaryFile(
      file -> new FileOutputStream(file.toFile()).write("great success".getBytes()),
      expectedFile);

    Assertions.assertThat(expectedFile).hasContent("great success");
  }

  @Test
  void shouldOverwriteExistingFile(@TempDirectory.TempDir Path tempDir) throws IOException {
    Path expectedFile = tempDir.resolve("toBeOverwritten.txt");
    Files.createFile(expectedFile);

    withTemporaryFile(
      file -> new FileOutputStream(file.toFile()).write("great success".getBytes()),
      expectedFile);

    Assertions.assertThat(expectedFile).hasContent("great success");
  }

  @Test
  void shouldFailForDirectory(@TempDirectory.TempDir Path tempDir) {
    assertThrows(IllegalArgumentException.class,
      () -> withTemporaryFile(
        file -> new FileOutputStream(file.toFile()).write("should not be written".getBytes()),
        tempDir));
  }

  @Test
  void shouldFailForMissingDirectory() {
    assertThrows(
      IllegalArgumentException.class,
      () -> withTemporaryFile(
        file -> new FileOutputStream(file.toFile()).write("should not be written".getBytes()),
        Paths.get("someFile")));
  }

  @Test
  void shouldKeepBackupIfTemporaryFileCouldNotBeWritten(@TempDirectory.TempDir Path tempDir) throws IOException {
    Path unchangedOriginalFile = tempDir.resolve("notToBeDeleted.txt");
    new FileOutputStream(unchangedOriginalFile.toFile()).write("this should be kept".getBytes());

    assertThrows(
      StoreException.class,
      () -> withTemporaryFile(
        file -> {
          throw new IOException("test");
        },
        unchangedOriginalFile));

    Assertions.assertThat(unchangedOriginalFile).hasContent("this should be kept");
  }

  @Test
  void shouldNotWrapRuntimeExceptions(@TempDirectory.TempDir Path tempDir) throws IOException {
    Path someFile = tempDir.resolve("something.txt");

    assertThrows(
      NullPointerException.class,
      () -> withTemporaryFile(
        file -> {
          throw new NullPointerException("test");
        },
        someFile));
  }

  @Test
  void shouldKeepBackupIfTemporaryFileIsMissing(@TempDirectory.TempDir Path tempDir) throws IOException {
    Path backedUpFile = tempDir.resolve("notToBeDeleted.txt");
    new FileOutputStream(backedUpFile.toFile()).write("this should be kept".getBytes());

    assertThrows(
      StoreException.class,
      () -> withTemporaryFile(
        Files::delete,
        backedUpFile));

    Assertions.assertThat(backedUpFile).hasContent("this should be kept");
  }

  @Test
  void shouldDeleteExistingFile(@TempDirectory.TempDir Path tempDir) throws IOException {
    Path expectedFile = tempDir.resolve("toBeReplaced.txt");
    new FileOutputStream(expectedFile.toFile()).write("this should be removed".getBytes());

    withTemporaryFile(
      file -> new FileOutputStream(file.toFile()).write("overwritten".getBytes()),
      expectedFile);

    Assertions.assertThat(Files.list(tempDir)).hasSize(1);
  }
}
