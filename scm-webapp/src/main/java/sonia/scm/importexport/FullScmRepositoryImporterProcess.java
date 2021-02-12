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

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ContextEntry;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.api.ImportFailedException;

import javax.inject.Inject;
import java.io.IOException;

import static java.util.Arrays.stream;

class FullScmRepositoryImporterProcess {

  private static final Logger LOG = LoggerFactory.getLogger(FullScmRepositoryImporterProcess.class);

  private final ImportStep[] importSteps;
  private final RepositoryManager repositoryManager;

  @Inject
  FullScmRepositoryImporterProcess(EnvironmentCheckStep environmentCheckStep,
                                   MetadataImportStep metadataImportStep,
                                   StoreImportStep storeImportStep,
                                   RepositoryImportStep repositoryImportStep,
                                   RepositoryManager repositoryManager
  ) {
    this.repositoryManager = repositoryManager;
    importSteps = new ImportStep[]{environmentCheckStep, metadataImportStep, storeImportStep, repositoryImportStep};
  }

  Repository run(Repository repository, TarArchiveInputStream tais) throws IOException {
    ImportState state = new ImportState(repositoryManager.create(repository));
    try {
      TarArchiveEntry tarArchiveEntry;
      while ((tarArchiveEntry = tais.getNextTarEntry()) != null) {
        TarArchiveEntry currentEntry = tarArchiveEntry;
        LOG.trace("Trying to handle tar entry '{}'", currentEntry.getName());
        stream(importSteps)
          .filter(step -> step.handle(currentEntry, state, tais))
          .findFirst()
          .orElseThrow(() -> new ImportFailedException(
            ContextEntry.ContextBuilder.entity(repository).build(),
            "Invalid import format. Unknown file in tar: " + currentEntry.getName()
          ));
      }
      stream(importSteps).forEach(step -> step.finish(state));
      return state.getRepository();
    } finally {
      stream(importSteps).forEach(step -> step.cleanup(state));
      if (!state.success()) {
        // Delete the repository if any error occurs during the import
        repositoryManager.delete(state.getRepository());
      }
    }
  }
}
