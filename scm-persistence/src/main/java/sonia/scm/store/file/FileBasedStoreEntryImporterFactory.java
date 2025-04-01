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

import sonia.scm.ContextEntry;
import sonia.scm.repository.api.ImportFailedException;
import sonia.scm.store.StoreEntryImporter;
import sonia.scm.store.StoreEntryImporterFactory;
import sonia.scm.store.StoreEntryMetaData;
import sonia.scm.store.StoreType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileBasedStoreEntryImporterFactory implements StoreEntryImporterFactory {

  private final Path directory;

  public FileBasedStoreEntryImporterFactory(Path directory) {
    this.directory = directory;
  }

  @Override
  public StoreEntryImporter importStore(StoreEntryMetaData metaData) {
    StoreType storeType = metaData.getType();
    String storeName = metaData.getName();
    Path storeDirectory = directory.resolve(Store.STORE_DIRECTORY);
    try {
      storeDirectory = storeDirectory.resolve(resolveFilePath(storeType.getValue(), storeName));
      Files.createDirectories(storeDirectory);
      if (!Files.exists(storeDirectory)) {
        throw new ImportFailedException(
          ContextEntry.ContextBuilder.noContext(),
          String.format("Could not create store for type %s and name %s", storeType, storeName)
        );
      }
      return new FileBasedStoreEntryImporter(storeDirectory);

    } catch (IOException e) {
      throw new ImportFailedException(
        ContextEntry.ContextBuilder.noContext(),
        String.format("Could not create store directory %s for type %s and name %s", storeDirectory, storeType, storeName)
      );
    }
  }

  private Path resolveFilePath(String type, String name) {
    if (name == null || name.isEmpty()) {
      return Paths.get(type);
    }
    return Paths.get(type, name);
  }
}
