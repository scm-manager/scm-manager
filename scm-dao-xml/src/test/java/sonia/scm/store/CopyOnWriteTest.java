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
