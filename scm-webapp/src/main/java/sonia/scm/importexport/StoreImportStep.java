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
