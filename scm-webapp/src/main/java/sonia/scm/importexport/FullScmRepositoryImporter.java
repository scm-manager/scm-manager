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

import com.google.common.base.Strings;
import jakarta.inject.Inject;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ContextEntry;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.ClearRepositoryCacheEvent;
import sonia.scm.repository.FullRepositoryImporter;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryImportEvent;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.api.ImportFailedException;
import sonia.scm.update.UpdateEngine;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
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
  private final UpdateEngine updateEngine;

  @Inject
  FullScmRepositoryImporter(EnvironmentCheckStep environmentCheckStep,
                                   MetadataImportStep metadataImportStep,
                                   StoreImportStep storeImportStep,
                                   QueryableStoreImportStep queryableStoreImportStep,
                                   RepositoryImportStep repositoryImportStep,
                                   RepositoryManager repositoryManager,
                                   RepositoryImportExportEncryption repositoryImportExportEncryption,
                                   RepositoryImportLoggerFactory loggerFactory,
                                   ScmEventBus eventBus,
                                   UpdateEngine updateEngine) {
    this.repositoryManager = repositoryManager;
    this.loggerFactory = loggerFactory;
    this.repositoryImportExportEncryption = repositoryImportExportEncryption;
    this.eventBus = eventBus;
    this.updateEngine = updateEngine;
    importSteps = new ImportStep[]{
      environmentCheckStep,
      metadataImportStep,
      storeImportStep,
      queryableStoreImportStep,
      repositoryImportStep
    };
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
    repository.setPermissions(singletonList(
      new RepositoryPermission(SecurityUtils.getSubject().getPrincipal().toString(), "OWNER", false)
    ));
    Repository createdRepository = repositoryManager.create(repository);

    RepositoryImportLogger logger = startLogger(repository);
    ImportState state = new ImportState(createdRepository, logger);
    logger.repositoryCreated(state.getRepository());
    try {
      TarArchiveEntry tarArchiveEntry;
      while ((tarArchiveEntry = tais.getNextEntry()) != null) {
        LOG.trace("Trying to handle tar entry '{}'", tarArchiveEntry.getName());
        handle(tais, state, tarArchiveEntry);
      }

      stream(importSteps).forEach(step -> step.finish(state));

      eventBus.post(new ClearRepositoryCacheEvent(createdRepository));
      updateEngine.update(repository.getId());
      eventBus.post(new ClearRepositoryCacheEvent(createdRepository));

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
