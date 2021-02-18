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

package sonia.scm.util;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import sonia.scm.ContextEntry;
import sonia.scm.repository.api.ExportFailedException;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class Archives {

  private Archives() {
  }

  /**
   * Creates a tar output stream that is backed by the given output stream.
   * @param dest The stream the tar should be written to.
   */
  public static TarArchiveOutputStream createTarOutputStream(OutputStream dest) {
    BufferedOutputStream bos = new BufferedOutputStream(dest);
    TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(bos);
    tarArchiveOutputStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
    tarArchiveOutputStream.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
    return tarArchiveOutputStream;
  }

  /**
   * Creates a tar input stream that takes its bytes from the given input stream.
   * @param source The stream the tar should be extracted from.
   */
  public static TarArchiveInputStream createTarInputStream(InputStream source) {
    return new TarArchiveInputStream(source);
  }

  public static TarWriter addPathToTar(Path path, OutputStream outputStream) {
    return new TarWriter(path, outputStream);
  }

  public static TarExtractor extractTar(InputStream inputStream, Path targetPath) {
    return new TarExtractor(inputStream, targetPath);
  }

  public static class TarWriter {
    private final Path path;
    private final OutputStream outputStream;

    private String basePath = "";
    private Predicate<Path> filter = path -> true;

    TarWriter(Path path, OutputStream outputStream) {
      this.path = path;
      this.outputStream = outputStream;
    }

    public TarWriter withBasePath(String basePath) {
      this.basePath = basePath;
      return this;
    }

    public TarWriter withFilter(Predicate<Path> filter) {
      this.filter = filter;
      return this;
    }

    public void run() throws IOException {
      try (TarArchiveOutputStream tarArchiveOutputStream = Archives.createTarOutputStream(outputStream)) {
        createTarEntryForFiles(basePath, path, tarArchiveOutputStream);
        tarArchiveOutputStream.finish();
      }
    }

    private void createTarEntryForFiles(String path, Path fileOrDir, TarArchiveOutputStream taos) throws IOException {
      try (Stream<Path> files = Files.list(fileOrDir)) {
        if (files != null) {
          files
            .filter(filter)
            .forEach(f -> bundleFileOrDir(path, f, taos));
        }
      }
    }

    private void bundleFileOrDir(String path, Path fileOrDir, TarArchiveOutputStream taos) {
      try {
        String filePath = path + "/" + fileOrDir.getFileName().toString();
        if (Files.isDirectory(fileOrDir)) {
          createTarEntryForFiles(filePath, fileOrDir, taos);
        } else {
          createArchiveEntryForFile(filePath, fileOrDir, taos);
        }
      } catch (IOException e) {
        throw new ExportFailedException(
          ContextEntry.ContextBuilder.noContext(),
          "Could not export repository. Error on bundling files.",
          e
        );
      }
    }

    private void createArchiveEntryForFile(String filePath, Path path, TarArchiveOutputStream taos) throws IOException {
      TarArchiveEntry entry = new TarArchiveEntry(filePath);
      entry.setSize(path.toFile().length());
      taos.putArchiveEntry(entry);
      Files.copy(path, taos);
      taos.closeArchiveEntry();
    }
  }

  public static class TarExtractor {
    private final InputStream inputStream;
    private final Path targetPath;

    public TarExtractor(InputStream inputStream, Path targetPath) {
      this.inputStream = inputStream;
      this.targetPath = targetPath;
    }

    public void run() throws IOException {
      try (TarArchiveInputStream tais = createTarInputStream(inputStream)) {
        TarArchiveEntry entry;
        while ((entry = tais.getNextTarEntry()) != null) {
          Path filePath = targetPath.resolve(entry.getName());
          createDirectoriesIfNestedFile(filePath);
          Files.copy(tais, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
      }
    }

    private void createDirectoriesIfNestedFile(Path filePath) throws IOException {
      Path directory = filePath.getParent();
      if (!Files.exists(directory)) {
        Files.createDirectories(directory);
      }
    }
  }
}
