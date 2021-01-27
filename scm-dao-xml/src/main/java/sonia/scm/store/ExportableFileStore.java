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

import sonia.scm.repository.api.ExportFailedException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static sonia.scm.ContextEntry.ContextBuilder.noContext;

public class ExportableFileStore implements ExportableStore {

  private final Path directory;
  private final StoreType type;

  public ExportableFileStore(Path directory, StoreType type) {
    this.directory = directory;
    this.type = type;
  }

  @Override
  public StoreEntryMetaData getMetaData() {
    return new StoreEntryMetaData(type, directory.getFileName().toString());
  }

  @Override
  public void export(Exporter exporter) throws IOException {
    exportDirectoryEntries(exporter, directory);
  }

  private void exportDirectoryEntries(Exporter exporter, Path directory) {
    if (Files.isDirectory(directory)) {
      try (Stream<Path> fileList = Files.list(directory)) {
        fileList.forEach(
          fileOrDir -> {
            if (Files.isDirectory(fileOrDir)) {
              exportDirectoryEntries(exporter, fileOrDir);
            } else if (shouldIncludeFile(fileOrDir)) {
              putFileContentIntoStream(exporter, fileOrDir);
            }
          }
        );
      } catch (IOException e) {
        throw new ExportFailedException(
          noContext(),
          "Could not read directory " + directory,
          e
        );
      }
    }
  }

  private boolean shouldIncludeFile(Path fileOrDir) {
    if (type.equals(StoreType.CONFIG) || type.equals(StoreType.DATA)) {
      return fileOrDir.getFileName().toString().endsWith(".xml");
    }
    if (type.equals(StoreType.BLOB)) {
      return fileOrDir.getFileName().toString().endsWith(".blob");
    }
    return false;
  }

  private void putFileContentIntoStream(Exporter exporter, Path file) {
    try (OutputStream stream = exporter.put(file.getFileName().toString(), Files.size(file))) {
      Files.copy(file, stream);
    } catch (IOException e) {
      throw new ExportFailedException(
        noContext(),
        "Could not copy file to export stream: " + file,
        e
      );
    }
  }
}
