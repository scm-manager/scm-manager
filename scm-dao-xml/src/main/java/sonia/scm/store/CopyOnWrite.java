package sonia.scm.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public final class CopyOnWrite {

  private static final Logger LOG = LoggerFactory.getLogger(CopyOnWrite.class);

  private CopyOnWrite() {}

  public static void withTemporaryFile(FileWriter writer, Path targetFile) {
    validateInput(targetFile);
    Path temporaryFile = createTemporaryFile(targetFile);
    executeCallback(writer, targetFile, temporaryFile);
    replaceOriginalFile(targetFile, temporaryFile);
  }

  @SuppressWarnings("squid:S3725") // performance of Files#isDirectory
  private static void validateInput(Path targetFile) {
    if (Files.isDirectory(targetFile)) {
      throw new IllegalArgumentException("target file has to be a regular file, not a directory");
    }
    if (targetFile.getParent() == null) {
      throw new IllegalArgumentException("target file has to be specified with a parent directory");
    }
  }

  private static Path createTemporaryFile(Path targetFile) {
    Path temporaryFile = targetFile.getParent().resolve(UUID.randomUUID().toString());
    try {
      Files.createFile(temporaryFile);
    } catch (IOException ex) {
      LOG.error("Error creating temporary file {} to replace file {}", temporaryFile, targetFile);
      throw new StoreException("could not create temporary file", ex);
    }
    return temporaryFile;
  }

  private static void executeCallback(FileWriter writer, Path targetFile, Path temporaryFile) {
    try {
      writer.write(temporaryFile);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception ex) {
      LOG.error("Error writing to temporary file {}. Target file {} has not been modified", temporaryFile, targetFile);
      throw new StoreException("could not write temporary file", ex);
    }
  }

  private static void replaceOriginalFile(Path targetFile, Path temporaryFile) {
    Path backupFile = backupOriginalFile(targetFile);
    try {
      Files.move(temporaryFile, targetFile);
    } catch (IOException e) {
      LOG.error("Error renaming temporary file {} to target file {}", temporaryFile, targetFile);
      restoreBackup(targetFile, backupFile);
      throw new StoreException("could rename temporary file to target file", e);
    }
    deleteBackupFile(backupFile);
  }

  @SuppressWarnings("squid:S3725") // performance of Files#exists
  private static Path backupOriginalFile(Path targetFile) {
    Path directory = targetFile.getParent();
    if (Files.exists(targetFile)) {
      Path backupFile = directory.resolve(UUID.randomUUID().toString());
      try {
        Files.move(targetFile, backupFile);
      } catch (IOException e) {
        LOG.error("Could not backup original file {}. Aborting here so that original file will not be overwritten.", targetFile);
        throw new StoreException("could not create backup of file", e);
      }
      return backupFile;
    } else {
      return null;
    }
  }

  private static void deleteBackupFile(Path backupFile) {
    if (backupFile != null) {
      try {
        Files.delete(backupFile);
      } catch (IOException e) {
        LOG.warn("Could not delete backup file {}", backupFile);
        throw new StoreException("could not delete backup file", e);
      }
    }
  }

  private static void restoreBackup(Path targetFile, Path backupFile) {
    if (backupFile != null) {
      try {
        Files.move(backupFile, targetFile);
        LOG.info("Recovered original file {} from backup", targetFile);
      } catch (IOException e) {
        LOG.error("Could not replace original file {} with backup file {} after failure", targetFile, backupFile);
      }
    }
  }

  @FunctionalInterface
  public interface FileWriter {
    @SuppressWarnings("squid:S00112") // We do not want to limit exceptions here
    void write(Path t) throws Exception;
  }
}
