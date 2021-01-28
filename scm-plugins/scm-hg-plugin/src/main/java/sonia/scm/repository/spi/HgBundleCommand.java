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

package sonia.scm.repository.spi;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import sonia.scm.repository.api.BundleResponse;

import javax.ws.rs.WebApplicationException;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class HgBundleCommand implements BundleCommand {

  private static final String TAR_ARCHIVE = "tar";
  private final HgCommandContext context;


  public HgBundleCommand(HgCommandContext context) {
    this.context = context;
  }

  @Override
  public BundleResponse bundle(BundleCommandRequest request) throws IOException {
    Path repoDir = context.getDirectory().toPath();
    if (Files.exists(repoDir)) {
      try (OutputStream os = request.getArchive().openStream();
           BufferedOutputStream bos = new BufferedOutputStream(os);
           TarArchiveOutputStream taos = new TarArchiveOutputStream(bos)) {

        createTarEntryForFiles("", repoDir, taos);
        taos.finish();
      }
    } else {
      //TODO throw ExportFailedException which already exists on the branch "feature/import_export_with_metadata"
    }
    return new BundleResponse(0);
  }

  //TODO Activate after git import export was merged
  //@Override
  //public String getFileExtension() {
  //  return TAR_ARCHIVE;
  //}

  private void createTarEntryForFiles(String path, Path fileOrDir, TarArchiveOutputStream taos) throws IOException {
    try (Stream<Path> files = Files.list(fileOrDir)) {
      if (files != null) {
        files
          .filter(this::shouldIncludeFile)
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
      //TODO throw ExportFailedException which already exists on the branch "feature/import_export_with_metadata"
      throw new WebApplicationException();
    }
  }

  private boolean shouldIncludeFile(Path filePath) {
    return !filePath.getFileName().toString().equals("config");
  }

  private void createArchiveEntryForFile(String filePath, Path path, TarArchiveOutputStream taos) throws IOException {
    TarArchiveEntry entry = new TarArchiveEntry(filePath);
    entry.setSize(path.toFile().length());
    taos.putArchiveEntry(entry);
    Files.copy(path, taos);
    taos.closeArchiveEntry();
  }
}
