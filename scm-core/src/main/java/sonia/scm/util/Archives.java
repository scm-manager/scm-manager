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
        } else if (Files.isSymbolicLink(fileOrDir)) {
          createArchiveEntryForLink(filePath, fileOrDir, taos);
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

    private static void createArchiveEntryForLink(String filePath, Path path, TarArchiveOutputStream taos) throws IOException {
      String linkTarget = Files.readSymbolicLink(path).toString();
      TarArchiveEntry entry = new TarArchiveEntry(filePath, TarArchiveEntry.LF_SYMLINK);
      entry.setLinkName(linkTarget);
      taos.putArchiveEntry(entry);
      taos.closeArchiveEntry();
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
          if (entry.isDirectory()) {
            Files.createDirectories(filePath);
          } else if (entry.isSymbolicLink() || entry.isLink()) {
            String linkTarget = entry.getLinkName();
            if (linkTarget == null || linkTarget.isEmpty()) {
              throw new ArchiveException("Symbolic link entry '" + entry.getName() + "' has no target", null);
            }
            Files.createSymbolicLink(filePath, Path.of(linkTarget));
          } else {
            Files.copy(tais, filePath, StandardCopyOption.REPLACE_EXISTING);
          }
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
