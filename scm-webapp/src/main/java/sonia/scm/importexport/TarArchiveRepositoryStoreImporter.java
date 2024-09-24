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

package sonia.scm.importexport;

import jakarta.inject.Inject;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import sonia.scm.ContextEntry;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.ImportFailedException;
import sonia.scm.store.RepositoryStoreImporter;
import sonia.scm.store.StoreEntryMetaData;
import sonia.scm.store.StoreType;

import java.io.IOException;
import java.io.InputStream;

public class TarArchiveRepositoryStoreImporter {

  private final RepositoryStoreImporter repositoryStoreImporter;

  @Inject
  public TarArchiveRepositoryStoreImporter(RepositoryStoreImporter repositoryStoreImporter) {
    this.repositoryStoreImporter = repositoryStoreImporter;
  }

  public void importFromTarArchive(Repository repository, InputStream inputStream, RepositoryImportLogger logger) {
    try (TarArchiveInputStream tais = new NoneClosingTarArchiveInputStream(inputStream)) {
      ArchiveEntry entry = tais.getNextEntry();
      while (entry != null) {
        String[] entryPathParts = entry.getName().split("/");
        validateStorePath(repository, entryPathParts);
        importStoreByType(repository, tais, entryPathParts, logger);
        entry = tais.getNextEntry();
      }
    } catch (IOException e) {
      throw new ImportFailedException(ContextEntry.ContextBuilder.entity(repository).build(), "Could not import stores from metadata file.", e);
    }
  }

  private void importStoreByType(Repository repository, TarArchiveInputStream tais, String[] entryPathParts, RepositoryImportLogger logger) {
    String storeType = entryPathParts[1];
    String storeName = entryPathParts[2];
    if (isDataStore(storeType)) {
      logger.step("importing data store entry for store " + storeName);
      repositoryStoreImporter
        .doImport(repository)
        .importStore(new StoreEntryMetaData(StoreType.DATA, entryPathParts[2]))
        .importEntry(entryPathParts[3], tais);
    } else if (isConfigStore(storeType)){
      logger.step("importing data store entry for store " + storeName);
      repositoryStoreImporter
        .doImport(repository)
        .importStore(new StoreEntryMetaData(StoreType.CONFIG, ""))
        .importEntry(storeName, tais);
    } else if(isBlobStore(storeType)) {
      logger.step("importing blob store entry for store " + storeName);
      repositoryStoreImporter
        .doImport(repository)
        .importStore(new StoreEntryMetaData(StoreType.BLOB, storeName))
        .importEntry(entryPathParts[3], tais);
    }
  }

  private void validateStorePath(Repository repository, String[] entryPathParts) {
    if (!isValidStorePath(entryPathParts)) {
      throw new ImportFailedException(
        ContextEntry.ContextBuilder.entity(repository).build(),
        "Invalid store path in metadata file"
      );
    }
  }

  private boolean isValidStorePath(String[] entryPathParts) {
    //This prevents array out of bound exceptions
    if (entryPathParts.length > 1) {
      String storeType = entryPathParts[1];
      if (isDataStore(storeType) || isBlobStore(storeType)) {
        return entryPathParts.length == 4;
      }
      if (isConfigStore(storeType)) {
        return entryPathParts.length == 3;
      }
    }
    return false;
  }

  private boolean isBlobStore(String storeType) {
    return storeType.equals(StoreType.BLOB.getValue());
  }

  private boolean isDataStore(String storeType) {
    return storeType.equals(StoreType.DATA.getValue());
  }

  private boolean isConfigStore(String storeType) {
    return storeType.equals(StoreType.CONFIG.getValue()) || storeType.equals(StoreType.CONFIG_ENTRY.getValue());
  }

  static class NoneClosingTarArchiveInputStream extends TarArchiveInputStream {

    public NoneClosingTarArchiveInputStream(InputStream is) {
      super(is);
    }

    @Override
    public void close() throws IOException {
      // Do not close this input stream
    }
  }
}
