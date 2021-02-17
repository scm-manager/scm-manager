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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ContextEntry;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.ImportFailedException;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.work.WorkdirProvider;
import sonia.scm.util.IOUtil;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

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
    if (!currentEntry.isDirectory()) {
      if (state.isStoreImported()) {
        LOG.trace("Importing directly from tar stream (entry '{}')", currentEntry.getName());
        unbundleRepository(state, inputStream);
      } else {
        LOG.debug("Temporally storing tar entry '{}' in work dir", currentEntry.getName());
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
    try {
      unbundleRepository(state, Files.newInputStream(path));
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
      service.getUnbundleCommand().unbundle(new NoneClosingInputStream(is));
      state.repositoryImported();
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
