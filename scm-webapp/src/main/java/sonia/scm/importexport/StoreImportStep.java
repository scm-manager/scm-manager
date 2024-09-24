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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Repository;
import sonia.scm.update.UpdateEngine;

import java.io.InputStream;

import static sonia.scm.importexport.FullScmRepositoryExporter.STORE_DATA_FILE_NAME;

class StoreImportStep implements ImportStep {

  private static final Logger LOG = LoggerFactory.getLogger(StoreImportStep.class);

  private final TarArchiveRepositoryStoreImporter storeImporter;
  private final UpdateEngine updateEngine;

  @Inject
  StoreImportStep(TarArchiveRepositoryStoreImporter storeImporter, UpdateEngine updateEngine) {
    this.storeImporter = storeImporter;
    this.updateEngine = updateEngine;
  }

  @Override
  public boolean handle(TarArchiveEntry entry, ImportState state, InputStream inputStream) {
    if (entry.getName().equals(STORE_DATA_FILE_NAME) && !entry.isDirectory()) {
      LOG.trace("Importing store from tar");
      state.getLogger().step("importing stores");
      // Inside the repository tar archive stream is another tar archive.
      // The nested tar archive is wrapped in another TarArchiveInputStream inside the storeImporter
      importStores(state.getRepository(), inputStream, state.getLogger());
      state.storeImported();
      return true;
    }
    return false;
  }

  private void importStores(Repository repository, InputStream inputStream, RepositoryImportLogger logger) {
    storeImporter.importFromTarArchive(repository, inputStream, logger);
    updateEngine.update(repository.getId());
  }
}
