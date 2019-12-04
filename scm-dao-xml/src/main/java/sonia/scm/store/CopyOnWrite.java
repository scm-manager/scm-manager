package sonia.scm.store;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public final class CopyOnWrite {

  private CopyOnWrite() {}

  public static void withTemporaryFile(FileWriter creator, Path targetFile) {
    validateInput(targetFile);

    try {
      Path temporaryFile = Files.createFile(targetFile.getParent().resolve(UUID.randomUUID().toString()));

      creator.write(temporaryFile);

      replaceOriginalFile(targetFile, temporaryFile);
    } catch (Exception ex) {
      throw new StoreException("could not write file", ex);
    }
  }

  private static void validateInput(Path targetFile) {
    if (Files.isDirectory(targetFile)) {
      throw new IllegalArgumentException("target file has to be a regular file, not a directory");
    }
    if (targetFile.getParent() == null) {
      throw new IllegalArgumentException("target file has to be specified with a parent directory");
    }
  }

  private static void replaceOriginalFile(Path targetFile, Path temporaryFile) throws IOException {
    Path backupFile = backupOriginalFile(targetFile);
    try {
      Files.move(temporaryFile, targetFile);
      if (backupFile != null) {
        Files.delete(backupFile);
      }
    } catch (RuntimeException | IOException e) {
      restoreBackup(targetFile, backupFile);
      throw e;
    }
  }

  private static Path backupOriginalFile(Path targetFile) throws IOException {
    Path directory = targetFile.getParent();
    if (Files.exists(targetFile)) {
      Path backupFile = directory.resolve(UUID.randomUUID().toString());
      Files.move(targetFile, backupFile);
      return backupFile;
    } else {
      return null;
    }
  }

  private static void restoreBackup(Path targetFile, Path backupFile) throws IOException {
    if (backupFile != null) {
      Files.move(backupFile, targetFile);
    }
  }

  @FunctionalInterface
  public interface FileWriter {
    void write(Path t) throws Exception;
  }
}
