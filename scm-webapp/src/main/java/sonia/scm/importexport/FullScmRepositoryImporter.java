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
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import sonia.scm.ContextEntry;
import sonia.scm.importexport.RepositoryMetadataXmlGenerator.RepositoryMetadata;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.api.ImportFailedException;
import sonia.scm.repository.api.IncompatibleEnvironmentForImportException;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.work.WorkdirProvider;
import sonia.scm.update.UpdateEngine;

import javax.inject.Inject;
import javax.xml.bind.JAXB;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static sonia.scm.importexport.FullScmRepositoryExporter.METADATA_FILE_NAME;
import static sonia.scm.importexport.FullScmRepositoryExporter.SCM_ENVIRONMENT_FILE_NAME;
import static sonia.scm.importexport.FullScmRepositoryExporter.STORE_DATA_FILE_NAME;

public class FullScmRepositoryImporter {

  @SuppressWarnings("java:S115") // we like this name here
  private static final int _1_MB = 1000000;

  private final RepositoryServiceFactory serviceFactory;
  private final RepositoryManager repositoryManager;
  private final ScmEnvironmentCompatibilityChecker compatibilityChecker;
  private final TarArchiveRepositoryStoreImporter storeImporter;
  private final UpdateEngine updateEngine;
  private final WorkdirProvider workdirProvider;

  @Inject
  public FullScmRepositoryImporter(RepositoryServiceFactory serviceFactory,
                                   RepositoryManager repositoryManager,
                                   ScmEnvironmentCompatibilityChecker compatibilityChecker,
                                   TarArchiveRepositoryStoreImporter storeImporter,
                                   UpdateEngine updateEngine,
                                   WorkdirProvider workdirProvider) {
    this.serviceFactory = serviceFactory;
    this.repositoryManager = repositoryManager;
    this.compatibilityChecker = compatibilityChecker;
    this.storeImporter = storeImporter;
    this.updateEngine = updateEngine;
    this.workdirProvider = workdirProvider;
  }

  public Repository importFromStream(Repository repository, InputStream inputStream) {
    Repository createdRepository = null;
    try {
      if (inputStream.available() > 0) {
        try (
          BufferedInputStream bif = new BufferedInputStream(inputStream);
          GzipCompressorInputStream gcis = new GzipCompressorInputStream(bif);
          TarArchiveInputStream tais = new TarArchiveInputStream(gcis)
        ) {
          checkScmEnvironment(repository, tais);
          Collection<RepositoryPermission> importedPermissions = processRepositoryMetadata(tais);
          createdRepository = repositoryManager.create(repository);
          importRepository(createdRepository, tais);
          importRepositoryPermissions(createdRepository, importedPermissions);
          return createdRepository;
        } catch (Exception e) {
          // Delete the repository if any error occurs during the import
          repositoryManager.delete(createdRepository);
          throw e;
        }
      } else {
        throw new ImportFailedException(
          ContextEntry.ContextBuilder.entity(repository).build(),
          "Stream to import from is empty."
        );
      }
    } catch (IOException e) {
      throw new ImportFailedException(
        ContextEntry.ContextBuilder.entity(repository).build(),
        "Could not import repository data from stream; got io exception while reading",
        e
      );
    }
  }

  private void importRepository(Repository createdRepository, TarArchiveInputStream tais) throws IOException {
    Optional<Path> savedRepositoryPath = importStoresOrSaveRepository(createdRepository, tais);
    if (savedRepositoryPath.isPresent()) {
      unbundleRepository(createdRepository, new FileInputStream(savedRepositoryPath.get().toFile()));
      Files.delete(savedRepositoryPath.get());
    } else {
      unbundleRepositoryFromTarArchiveInputStream(createdRepository, tais);
    }
  }

  private void importRepositoryPermissions(Repository repository, Collection<RepositoryPermission> importedPermissions) {
    Collection<RepositoryPermission> existingPermissions = repository.getPermissions();
    RepositoryImportPermissionMerger permissionMerger = new RepositoryImportPermissionMerger();
    Collection<RepositoryPermission> permissions = permissionMerger.merge(existingPermissions, importedPermissions);
    repository.setPermissions(permissions);
    repositoryManager.modify(repository);
  }

  private Optional<Path> importStoresOrSaveRepository(Repository repository, TarArchiveInputStream tais) throws IOException {
    ArchiveEntry entry = tais.getNextEntry();
    if (entry.getName().equals(STORE_DATA_FILE_NAME) && !entry.isDirectory()) {
      // Inside the repository tar archive stream is another tar archive.
      // The nested tar archive is wrapped in another TarArchiveInputStream inside the storeImporter
      importStores(repository, tais);
      return Optional.empty();
    } else if (!entry.isDirectory()) {
      Path path = saveRepositoryDataFromTarArchiveEntry(repository, tais);
      importStores(repository, tais);
      return Optional.of(path);
    } else {
      throw new ImportFailedException(
        ContextEntry.ContextBuilder.entity(repository).build(),
        "Invalid import format. Missing metadata file 'scm-metadata.tar' in tar."
      );
    }
  }

  private void importStores(Repository repository, TarArchiveInputStream tais) {
    storeImporter.importFromTarArchive(repository, tais);
    updateEngine.update(repository.getId());
  }

  private Path saveRepositoryDataFromTarArchiveEntry(Repository repository, TarArchiveInputStream tais) throws IOException {
    // The order of files inside the repository archives was changed.
    // Due to ensure backwards compatible with existing repository archives we save the repository
    // and read it again after the stores were imported.
    Path repositoryPath = createSavedRepositoryLocation(repository);
    Files.copy(tais, repositoryPath);
    return repositoryPath;
  }

  private Path createSavedRepositoryLocation(Repository repository) {
    return workdirProvider.createNewWorkdir(repository.getId()).toPath().resolve("repository");
  }

  private void unbundleRepositoryFromTarArchiveInputStream(Repository repository, TarArchiveInputStream tais) throws IOException {
    ArchiveEntry repositoryEntry = tais.getNextEntry();
    if (!repositoryEntry.isDirectory()) {
      unbundleRepository(repository, tais);
    } else {
      throw new ImportFailedException(
        ContextEntry.ContextBuilder.entity(repository).build(),
        "Invalid import format. Missing repository dump file."
      );
    }
  }

  private void unbundleRepository(Repository repository, InputStream is) {
    try (RepositoryService service = serviceFactory.create(repository)) {
      service.getUnbundleCommand().unbundle(new NoneClosingInputStream(is));
    } catch (IOException e) {
      throw new ImportFailedException(
        ContextEntry.ContextBuilder.entity(repository).build(),
        "Repository import failed. Could not import repository from file.",
        e
      );
    }
  }

  private void checkScmEnvironment(Repository repository, TarArchiveInputStream tais) throws IOException {
    ArchiveEntry environmentEntry = tais.getNextEntry();
    if (environmentEntry.getName().equals(SCM_ENVIRONMENT_FILE_NAME) && !environmentEntry.isDirectory() && environmentEntry.getSize() < _1_MB) {
      boolean validEnvironment = compatibilityChecker.check(JAXB.unmarshal(new NoneClosingInputStream(tais), ScmEnvironment.class));
      if (!validEnvironment) {
        throw new IncompatibleEnvironmentForImportException();
      }
    } else {
      throw new ImportFailedException(
        ContextEntry.ContextBuilder.entity(repository).build(),
        "Invalid import format. Missing SCM-Manager environment description file 'scm-environment.xml' or file too big."
      );
    }
  }

  private Collection<RepositoryPermission> processRepositoryMetadata(TarArchiveInputStream tais) throws IOException {
    ArchiveEntry metadataEntry = tais.getNextEntry();
    if (metadataEntry.getName().equals(METADATA_FILE_NAME)) {
      RepositoryMetadata metadata = JAXB.unmarshal(new NoneClosingInputStream(tais), RepositoryMetadata.class);
      if (metadata != null && metadata.getPermissions() != null) {
        return new HashSet<>(metadata.getPermissions());
      }
      return Collections.emptySet();
    } else {
      throw new ImportFailedException(
        ContextEntry.ContextBuilder.noContext(),
        String.format("Invalid import format. Missing SCM-Manager metadata description file %s.", METADATA_FILE_NAME)
      );
    }
  }

  @SuppressWarnings("java:S4929") // we only want to override close here
  static class NoneClosingInputStream extends FilterInputStream {

    NoneClosingInputStream(InputStream delegate) {
      super(delegate);
    }

    @Override
    public void close() {
      // Avoid closing stream because JAXB tries to close the stream
    }
  }
}
