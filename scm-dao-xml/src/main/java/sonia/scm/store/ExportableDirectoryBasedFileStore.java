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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static sonia.scm.ContextEntry.ContextBuilder.noContext;
import static sonia.scm.store.ExportCopier.putFileContentIntoStream;

abstract class ExportableDirectoryBasedFileStore implements ExportableStore {

  private final Path directory;

  ExportableDirectoryBasedFileStore(Path directory) {
    this.directory = directory;
  }

  @Override
  public StoreEntryMetaData getMetaData() {
    return new StoreEntryMetaData(getStoreType(), directory.getFileName().toString());
  }

  abstract StoreType getStoreType();

  abstract boolean shouldIncludeFile(Path file);

  @Override
  public void export(Exporter exporter) throws IOException {
    exportDirectoryEntries(exporter, directory);
  }

  private void exportDirectoryEntries(Exporter exporter, Path directory) {
    try (Stream<Path> fileList = Files.list(directory)) {
      fileList.forEach(fileOrDir -> exportIfRelevant(exporter, fileOrDir));
    } catch (IOException e) {
      throw new ExportFailedException(
        noContext(),
        "Could not read directory " + directory,
        e
      );
    }
  }

  private void exportIfRelevant(Exporter exporter, Path fileOrDir) {
    if (!Files.isDirectory(fileOrDir) && shouldIncludeFile(fileOrDir)) {
      putFileContentIntoStream(exporter, fileOrDir);
    }
  }

  protected Path getDirectory() {
    return directory;
  }
}
