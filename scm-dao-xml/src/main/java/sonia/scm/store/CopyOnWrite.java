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

import com.google.common.util.concurrent.Striped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.locks.Lock;

/**
 * CopyOnWrite creates a copy of the target file, before it is modified. This should prevent empty or incomplete files
 * on errors such as full disk.
 * <p>
 * javasecurity:S2083: SonarQube thinks that the path (targetFile) is generated from an http header (HttpUtil), but
 * this is not true. It looks like a false-positive, so we suppress the warning for now.
 */
@SuppressWarnings("javasecurity:S2083")
public final class CopyOnWrite {

  private static final Logger LOG = LoggerFactory.getLogger(CopyOnWrite.class);

  @SuppressWarnings("UnstableApiUsage")
  private static final Striped<Lock> concurrencyLock = Striped.lock(10);

  private CopyOnWrite() {
  }

  public static void withTemporaryFile(FileWriter writer, Path targetFile) {
    validateInput(targetFile);
    Lock lock = concurrencyLock.get(targetFile.toUri().toString());
    try {
      lock.lock();
      Path temporaryFile = createTemporaryFile(targetFile);
      executeCallback(writer, targetFile, temporaryFile);
      replaceOriginalFile(targetFile, temporaryFile);
    } finally {
      lock.unlock();
    }
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
