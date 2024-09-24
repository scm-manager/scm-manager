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
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ContextEntry;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.ExportFailedException;
import sonia.scm.store.ExportableStore;
import sonia.scm.store.StoreEntryMetaData;
import sonia.scm.store.StoreExporter;
import sonia.scm.store.StoreType;
import sonia.scm.util.Archives;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static java.util.Arrays.asList;

public class TarArchiveRepositoryStoreExporter {

  private static final Logger LOG = LoggerFactory.getLogger(TarArchiveRepositoryStoreExporter.class);

  private final StoreExporter storeExporter;

  @Inject
  public TarArchiveRepositoryStoreExporter(StoreExporter storeExporter) {
    this.storeExporter = storeExporter;
  }

  public void export(Repository repository, OutputStream output) {
    try (
      final TarArchiveOutputStream taos = Archives.createTarOutputStream(output)
    ) {
      List<ExportableStore> exportableStores = storeExporter.listExportableStores(repository);
      for (ExportableStore store : exportableStores) {
        store.export((name, filesize) -> {
          StoreEntryMetaData storeMetaData = store.getMetaData();
          if (isOneOfStoreTypes(store, StoreType.DATA, StoreType.BLOB)) {
            String storePath = createStorePath(storeMetaData.getType().getValue(), storeMetaData.getName(), name);
            addEntryToArchive(taos, storePath, filesize);
          } else if (isOneOfStoreTypes(store, StoreType.CONFIG, StoreType.CONFIG_ENTRY)) {
            String storePath = createStorePath(storeMetaData.getType().getValue(), name);
            addEntryToArchive(taos, storePath, filesize);
          } else {
            LOG.debug("Skip file {} on export", name);
          }
          return createOutputStream(taos);
        });
      }

    } catch (IOException e) {
      throw new ExportFailedException(
        ContextEntry.ContextBuilder.entity(repository).build(),
        "Could not export repository metadata stores.",
        e
      );
    }
  }

  private boolean isOneOfStoreTypes(ExportableStore store, StoreType... types) {
    return asList(types).contains(store.getMetaData().getType());
  }

  private void addEntryToArchive(TarArchiveOutputStream taos, String storePath, long filesize) throws IOException {
    TarArchiveEntry entry = new TarArchiveEntry(storePath);
    entry.setSize(filesize);
    taos.putArchiveEntry(entry);
  }

  private String createStorePath(String... pathParts) {
    StringBuilder storePath = new StringBuilder("stores");
    for (String part : pathParts) {
      storePath.append('/').append(part);
    }
    return storePath.toString();
  }

  private OutputStream createOutputStream(TarArchiveOutputStream taos) {
    return new CloseArchiveOutputStream(taos);
  }

  static class CloseArchiveOutputStream extends FilterOutputStream {

    private final TarArchiveOutputStream delegate;

    CloseArchiveOutputStream(TarArchiveOutputStream delegate) {
      super(delegate);
      this.delegate = delegate;
    }

    @Override
    public void close() throws IOException {
      delegate.closeArchiveEntry();
    }
  }
}
