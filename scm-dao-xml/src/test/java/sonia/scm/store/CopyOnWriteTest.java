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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sonia.scm.store.CopyOnWrite.withTemporaryFile;

class CopyOnWriteTest {

  @Test
  void shouldCreateNewFile(@TempDir Path tempDir) {
    Path expectedFile = tempDir.resolve("toBeCreated.txt");

    withTemporaryFile(file -> {
      try (OutputStream os = new FileOutputStream(file.toFile())) {
        os.write("great success".getBytes());
      }
    }, expectedFile);

    assertThat(expectedFile).hasContent("great success");
  }

  @Test
  void shouldOverwriteExistingFile(@TempDir Path tempDir) throws IOException {
    Path expectedFile = tempDir.resolve("toBeOverwritten.txt");
    Files.createFile(expectedFile);

    withTemporaryFile(file -> {
      try (OutputStream os = new FileOutputStream(file.toFile())) {
        os.write("great success".getBytes());
      }
    }, expectedFile);

    assertThat(expectedFile).hasContent("great success");
  }

  @Test
  void shouldFailForDirectory(@TempDir Path tempDir) {
    assertThrows(IllegalArgumentException.class, () -> withTemporaryFile(file -> {
        try (OutputStream os = new FileOutputStream(file.toFile())) {
          os.write("should not be written".getBytes());
        }
      }, tempDir));
  }

  @Test
  void shouldFailForMissingDirectory() {
    assertThrows(IllegalArgumentException.class, () -> withTemporaryFile(file -> {
      try (OutputStream os = new FileOutputStream(file.toFile())) {
        os.write("should not be written".getBytes());
      }
    }, Paths.get("someFile")));
  }

  @Test
  void shouldKeepBackupIfTemporaryFileCouldNotBeWritten(@TempDir Path tempDir) throws IOException {
    Path unchangedOriginalFile = tempDir.resolve("notToBeDeleted.txt");

    try (OutputStream unchangedOriginalOs = new FileOutputStream(unchangedOriginalFile.toFile())) {
      unchangedOriginalOs.write("this should be kept".getBytes());
    }

    assertThrows(
      StoreException.class,
      () -> withTemporaryFile(
        file -> {
          throw new IOException("test");
        },
        unchangedOriginalFile));

    assertThat(unchangedOriginalFile).hasContent("this should be kept");
  }

  @Test
  void shouldDeleteTemporaryFileIfFileCouldNotBeWritten(@TempDir Path tempDir) throws IOException {
    Path unchangedOriginalFile = tempDir.resolve("target.txt");

    assertThrows(
      StoreException.class,
      () -> withTemporaryFile(
        file -> {
          throw new IOException("test");
        },
        unchangedOriginalFile));

    assertThat(tempDir).isEmptyDirectory();
  }

  @Test
  void shouldNotWrapRuntimeExceptions(@TempDir Path tempDir) throws IOException {
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
  void shouldKeepBackupIfTemporaryFileIsMissing(@TempDir Path tempDir) throws IOException {
    Path backedUpFile = tempDir.resolve("notToBeDeleted.txt");

    try (OutputStream backedUpOs = new FileOutputStream(backedUpFile.toFile())) {
      backedUpOs.write("this should be kept".getBytes());
    }

    assertThrows(
      StoreException.class,
      () -> withTemporaryFile(
        Files::delete,
        backedUpFile));

    assertThat(backedUpFile).hasContent("this should be kept");
  }

  @Test
  void shouldDeleteExistingFile(@TempDir Path tempDir) throws IOException {
    Path expectedFile = tempDir.resolve("toBeReplaced.txt");

    try (OutputStream expectedOs = new FileOutputStream(expectedFile.toFile())) {
      expectedOs.write("this should be removed".getBytes());
    }

    withTemporaryFile(file -> {
      try (OutputStream os = new FileOutputStream(file.toFile())) {
        os.write("overwritten".getBytes()) ;
      }
    }, expectedFile);

    assertThat(Files.list(tempDir)).hasSize(1);
  }
}
