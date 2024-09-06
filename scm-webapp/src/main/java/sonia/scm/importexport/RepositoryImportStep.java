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
import sonia.scm.ContextEntry;
import sonia.scm.repository.ImportRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.api.ImportFailedException;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.api.UnbundleCommandBuilder;
import sonia.scm.repository.work.WorkdirProvider;
import sonia.scm.util.IOUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import static sonia.scm.ContextEntry.ContextBuilder.entity;

class RepositoryImportStep implements ImportStep {

  private static final Logger LOG = LoggerFactory.getLogger(RepositoryImportStep.class);

  private final RepositoryServiceFactory serviceFactory;
  private final WorkdirProvider workdirProvider;

  @Inject
  RepositoryImportStep(RepositoryServiceFactory serviceFactory, WorkdirProvider workdirProvider) {
    this.serviceFactory = serviceFactory;
    this.workdirProvider = workdirProvider;
  }

  @Override
  public boolean handle(TarArchiveEntry currentEntry, ImportState state, InputStream inputStream) {
    if (!currentEntry.isDirectory() && !currentEntry.getName().contains("/")) {
      if (state.isStoreImported()) {
        LOG.trace("Importing directly from tar stream (entry '{}')", currentEntry.getName());
        state.getLogger().step("directly importing repository data");
        unbundleRepository(state, inputStream);
      } else {
        LOG.debug("Temporarily storing tar entry '{}' in work dir", currentEntry.getName());
        state.getLogger().step("temporarily storing repository data for later import");
        Path path = saveRepositoryDataFromTarArchiveEntry(state.getRepository(), inputStream);
        state.setTemporaryRepositoryBundle(path);
      }
      return true;
    }
    return false;
  }

  @Override
  public void finish(ImportState state) {
    state.getTemporaryRepositoryBundle()
      .ifPresent(path -> importFromTemporaryPath(state, path));
  }

  @Override
  public void cleanup(ImportState state) {
    state.getTemporaryRepositoryBundle()
      .ifPresent(path -> IOUtil.deleteSilently(path.getParent().toFile()));
  }

  private void importFromTemporaryPath(ImportState state, Path path) {
    LOG.debug("Importing repository from temporary location in work dir");
    state.getLogger().step("importing repository from temporary location");
    try (InputStream is = Files.newInputStream(path)) {
      unbundleRepository(state, is);
    } catch (IOException e) {
      throw new ImportFailedException(
        entity(state.getRepository()).build(),
        "Repository import failed. Could not import repository from temporary file.",
        e
      );
    }
  }

  private void unbundleRepository(ImportState state, InputStream is) {
    try (RepositoryService service = serviceFactory.create(state.getRepository())) {
      AtomicReference<RepositoryHookEvent> eventSink = new AtomicReference<>();

      UnbundleCommandBuilder unbundleCommand = service.getUnbundleCommand();
      unbundleCommand.setPostEventSink(eventSink::set);
      unbundleCommand.unbundle(new NoneClosingInputStream(is));

      state.repositoryImported();

      RepositoryHookEvent repositoryHookEvent = eventSink.get();
      if (repositoryHookEvent != null) {
        state.addPendingEvent(new ImportRepositoryHookEvent(repositoryHookEvent));
      }
    } catch (IOException e) {
      throw new ImportFailedException(
        entity(state.getRepository()).build(),
        "Repository import failed. Could not import repository from file.",
        e
      );
    }
  }

  private Path saveRepositoryDataFromTarArchiveEntry(Repository repository, InputStream tais) {
    // The order of files inside the repository archives was changed.
    // Due to ensure backwards compatible with existing repository archives we save the repository
    // and read it again after the stores were imported.
    Path repositoryPath = createSavedRepositoryLocation(repository);
    try {
      Files.copy(tais, repositoryPath);
    } catch (IOException e) {
      throw new ImportFailedException(ContextEntry.ContextBuilder.noContext(), "Could not temporarilly store repository bundle", e);
    }
    return repositoryPath;
  }

  private Path createSavedRepositoryLocation(Repository repository) {
    return workdirProvider.createNewWorkdir(repository.getId()).toPath().resolve("repository");
  }
}
