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

package sonia.scm.store.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sonia.scm.store.StoreException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sonia.scm.CopyOnWrite.withTemporaryFile;

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
