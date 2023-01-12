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

import com.google.common.base.Strings;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ContextEntry;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.*;
import sonia.scm.repository.api.ImportFailedException;

import javax.inject.Inject;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.util.Arrays.stream;
import static sonia.scm.ContextEntry.ContextBuilder.noContext;
import static sonia.scm.importexport.RepositoryImportLogger.ImportType.FULL;
import static sonia.scm.util.Archives.createTarInputStream;

public class FullScmRepositoryImporter implements FullRepositoryImporter {

  private static final Logger LOG = LoggerFactory.getLogger(FullScmRepositoryImporter.class);

  private final ImportStep[] importSteps;
  private final RepositoryManager repositoryManager;
  private final RepositoryImportExportEncryption repositoryImportExportEncryption;
  private final ScmEventBus eventBus;
  private final RepositoryImportLoggerFactory loggerFactory;

  @Inject
  public FullScmRepositoryImporter(EnvironmentCheckStep environmentCheckStep,
                                   MetadataImportStep metadataImportStep,
                                   StoreImportStep storeImportStep,
                                   RepositoryImportStep repositoryImportStep,
                                   RepositoryManager repositoryManager,
                                   RepositoryImportExportEncryption repositoryImportExportEncryption,
                                   RepositoryImportLoggerFactory loggerFactory,
                                   ScmEventBus eventBus) {
    this.repositoryManager = repositoryManager;
    this.loggerFactory = loggerFactory;
    this.repositoryImportExportEncryption = repositoryImportExportEncryption;
    this.eventBus = eventBus;
    importSteps = new ImportStep[]{environmentCheckStep, metadataImportStep, storeImportStep, repositoryImportStep};
  }

  public Repository importFromStream(Repository repository, InputStream inputStream, String password) {
    RepositoryPermissions.create().check();
    try {
      if (inputStream.available() > 0) {
        try (
          BufferedInputStream bif = new BufferedInputStream(inputStream);
          InputStream cif = decryptIfPasswordSet(bif, password);
          GzipCompressorInputStream gcis = new GzipCompressorInputStream(cif);
          TarArchiveInputStream tais = createTarInputStream(gcis)
        ) {
          return run(repository, tais);
        }
      } else {
        throw new ImportFailedException(
          ContextEntry.ContextBuilder.entity(repository).build(),
          "Stream to import from is empty."
        );
      }
    } catch (IOException e) {
      throw new ImportFailedException(
        noContext(),
        "Could not import repository data from stream; got io exception while reading",
        e
      );
    }
  }

  private InputStream decryptIfPasswordSet(InputStream potentiallyEncryptedStream, String password) throws IOException {
    if (Strings.isNullOrEmpty(password)) {
      return potentiallyEncryptedStream;
    } else {
      return repositoryImportExportEncryption.decrypt(potentiallyEncryptedStream, password);
    }
  }

  private RepositoryImportLogger startLogger(Repository repository) {
    RepositoryImportLogger logger = loggerFactory.createLogger();
    logger.start(FULL, repository);
    return logger;
  }

  private Repository run(Repository repository, TarArchiveInputStream tais) throws IOException {
    Repository createdRepository = repositoryManager.create(repository);
    RepositoryImportLogger logger = startLogger(repository);
    ImportState state = new ImportState(createdRepository, logger);
    logger.repositoryCreated(state.getRepository());
    try {
      TarArchiveEntry tarArchiveEntry;
      while ((tarArchiveEntry = tais.getNextTarEntry()) != null) {
        LOG.trace("Trying to handle tar entry '{}'", tarArchiveEntry.getName());
        handle(tais, state, tarArchiveEntry);
      }
      stream(importSteps).forEach(step -> step.finish(state));
      state.getLogger().finished();
      return state.getRepository();
    } catch (RuntimeException | IOException e) {
      state.getLogger().failed(e);
      throw e;
    } finally {
      stream(importSteps).forEach(step -> step.cleanup(state));
      if (state.success()) {
        // send all pending events on successful import
        state.getPendingEvents().forEach(eventBus::post);
        eventBus.post(new RepositoryImportEvent(repository, false));
      } else {
        // Delete the repository if any error occurs during the import
        repositoryManager.delete(state.getRepository());
        eventBus.post(new RepositoryImportEvent(repository, true));
      }

    }
  }

  private void handle(TarArchiveInputStream tais, ImportState state, TarArchiveEntry currentEntry) {
    state.getLogger().step("inspecting file " + currentEntry.getName());
    for (ImportStep step : importSteps) {
      if (step.handle(currentEntry, state, tais)) {
        return;
      }
    }
    throw new ImportFailedException(
      ContextEntry.ContextBuilder.entity(state.getRepository()).build(),
      "Invalid import format. Unknown file in tar: " + currentEntry.getName()
    );
  }
}
