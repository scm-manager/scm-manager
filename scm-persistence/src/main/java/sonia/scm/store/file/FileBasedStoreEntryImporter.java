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

package sonia.scm.store.file;

import com.google.common.annotations.VisibleForTesting;
import sonia.scm.ContextEntry;
import sonia.scm.repository.api.ImportFailedException;
import sonia.scm.store.StoreEntryImporter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

class FileBasedStoreEntryImporter implements StoreEntryImporter {

  private final Path directory;

  FileBasedStoreEntryImporter(Path directory) {
    this.directory = directory;
  }

  @VisibleForTesting
  Path getDirectory() {
    return this.directory;
  }

  @Override
  public void importEntry(String name, InputStream stream) {
    Path filePath = directory.resolve(name);
    try {
      Files.copy(stream, filePath, REPLACE_EXISTING);
    } catch (IOException e) {
      throw new ImportFailedException(
        ContextEntry.ContextBuilder.noContext(),
        String.format("Could not import file %s for store %s", name, directory.toString()),
        e
      );
    }
  }
}
