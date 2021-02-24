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

package sonia.scm.importexport;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import sonia.scm.ContextEntry;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.ImportFailedException;
import sonia.scm.store.RepositoryStoreImporter;
import sonia.scm.store.StoreEntryMetaData;
import sonia.scm.store.StoreType;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class TarArchiveRepositoryStoreImporter {

  private final RepositoryStoreImporter repositoryStoreImporter;

  @Inject
  public TarArchiveRepositoryStoreImporter(RepositoryStoreImporter repositoryStoreImporter) {
    this.repositoryStoreImporter = repositoryStoreImporter;
  }

  public void importFromTarArchive(Repository repository, InputStream inputStream) {
    try (TarArchiveInputStream tais = new NoneClosingTarArchiveInputStream(inputStream)) {
      ArchiveEntry entry = tais.getNextEntry();
      while (entry != null) {
        String[] entryPathParts = entry.getName().split(File.separator);
        validateStorePath(repository, entryPathParts);
        importStoreByType(repository, tais, entryPathParts);
        entry = tais.getNextEntry();
      }
    } catch (IOException e) {
      throw  new ImportFailedException(ContextEntry.ContextBuilder.entity(repository).build(), "Could not import stores from metadata file.", e);
    }
  }

  private void importStoreByType(Repository repository, TarArchiveInputStream tais, String[] entryPathParts) {
    String storeType = entryPathParts[1];
    if (isDataStore(storeType)) {
      repositoryStoreImporter
        .doImport(repository)
        .importStore(new StoreEntryMetaData(StoreType.DATA, entryPathParts[2]))
        .importEntry(entryPathParts[3], tais);
    } else if (isConfigStore(storeType)){
      repositoryStoreImporter
        .doImport(repository)
        .importStore(new StoreEntryMetaData(StoreType.CONFIG, ""))
        .importEntry(entryPathParts[2], tais);
    } else if(isBlobStore(storeType)) {
      repositoryStoreImporter
        .doImport(repository)
        .importStore(new StoreEntryMetaData(StoreType.BLOB, entryPathParts[2]))
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
