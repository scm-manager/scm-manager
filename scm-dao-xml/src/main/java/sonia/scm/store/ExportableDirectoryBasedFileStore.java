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
