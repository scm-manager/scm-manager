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

  /**
   * Prepare bundling a complete {@link Path} in a tar. Aside from the path whose files shall be added to the tar,
   * it is possible to specify a base path for the tar archive ({@link TarWriter#withBasePath(String)}) and to
   * specify a filter to select what files shall be added ({@link TarWriter#withFilter(Predicate)}). To start
   * the process, {@link TarWriter#run()} has to be called.
   * <br>
   * Example:
   * <pre>
   *   Archives.addPathToTar(Paths.get("some/dir"), Files.newOutputStream("my.tar"))
   *     .filter(path -&gt; !path.toString().endsWith("~"))
   *     .run();
   * </pre>
   * @param path The path containing the files to be added to the tar.
   * @param outputStream The output stream the tar shall be written to.
   * @return Instance of {@link TarWriter}.
   */
  public static TarWriter addPathToTar(Path path, OutputStream outputStream) {
    return new TarWriter(path, outputStream);
  }

  /**
   * Extracts files from a input stream with a tar.
   * <br>
   * Example:
   * <pre>
   *   Archives.extractTar(Files.newOutputStream("my.tar"), Paths.get("target/dir"))
   *     .run();
   * </pre>
   * @param inputStream The input stream providing the tar stream.
   * @param targetPath The target path to write the files to.
   * @return Instance of {@link TarExtractor}.
   */
  public static TarExtractor extractTar(InputStream inputStream, Path targetPath) {
    return new TarExtractor(inputStream, targetPath);
  }

  /**
   * Class used in {@link #addPathToTar(Path, OutputStream)}.
   */
  public static class TarWriter {
    private final Path path;
    private final OutputStream outputStream;

    private String basePath = "";
    private Predicate<Path> filter = path -> true;

    TarWriter(Path path, OutputStream outputStream) {
      this.path = path;
      this.outputStream = outputStream;
    }

    /**
     * Sets a base path that will be prepended to every tar entry in the archive.
     */
    public TarWriter withBasePath(String basePath) {
      this.basePath = basePath;
      return this;
    }

    /**
     * Sets a filter to select which files shall be added to the archive.
     */
    public TarWriter withFilter(Predicate<Path> filter) {
      this.filter = filter;
      return this;
    }

    /**
     * Starts the process.
     */
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

    @SuppressWarnings("java:S1075") // We use / here because we build tar entries, not files
    private void bundleFileOrDir(String path, Path fileOrDir, TarArchiveOutputStream taos) {
      try {
        String filePath = path + "/" + fileOrDir.getFileName().toString();
        if (Files.isDirectory(fileOrDir)) {
          createTarEntryForFiles(filePath, fileOrDir, taos);
        } else {
          createArchiveEntryForFile(filePath, fileOrDir, taos);
        }
      } catch (IOException e) {
        throw new ArchiveException(
          "Could not add file '" + fileOrDir + "' to tar archive as '" + path + "'",
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

  /**
   * Classed used in {@link #extractTar(InputStream, Path)}.
   */
  public static class TarExtractor {
    private final InputStream inputStream;
    private final Path targetPath;

    TarExtractor(InputStream inputStream, Path targetPath) {
      this.inputStream = inputStream;
      this.targetPath = targetPath;
    }

    /**
     * Starts the process.
     */
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

  public static final class ArchiveException extends RuntimeException {
    public ArchiveException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
