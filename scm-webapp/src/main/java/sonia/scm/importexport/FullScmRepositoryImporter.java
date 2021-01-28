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
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.api.ImportFailedException;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import javax.inject.Inject;
import javax.xml.bind.JAXB;
import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FullScmRepositoryImporter {

  private static final int _1_MB = 1000000;
  private static final String SCM_ENVIRONMENT_FILE = "scm-environment.xml";
  private static final String SCM_METADATA_TAR_FILE = "scm-metadata.tar";

  private final RepositoryServiceFactory serviceFactory;
  private final RepositoryManager repositoryManager;
  private final ScmEnvironmentCompatibilityChecker compatibilityChecker;
  private final TarArchiveRepositoryStoreImporter storeImporter;

  @Inject
  public FullScmRepositoryImporter(RepositoryServiceFactory serviceFactory,
                                   RepositoryManager repositoryManager,
                                   ScmEnvironmentCompatibilityChecker compatibilityChecker,
                                   TarArchiveRepositoryStoreImporter storeImporter) {
    this.serviceFactory = serviceFactory;
    this.repositoryManager = repositoryManager;
    this.compatibilityChecker = compatibilityChecker;
    this.storeImporter = storeImporter;
  }

  public Repository importFromStream(Repository repository, InputStream inputStream) {
    try {
      if (inputStream.available() > 0) {
        try (
          BufferedInputStream bif = new BufferedInputStream(inputStream);
          GzipCompressorInputStream gcis = new GzipCompressorInputStream(bif);
          TarArchiveInputStream tais = new TarArchiveInputStream(gcis)
        ) {
          checkScmEnvironment(repository, tais);
          skipRepositoryMetadata(tais);
          Repository createdRepository = importRepositoryFromFile(repository, tais);
          importStoresForCreatedRepository(createdRepository, tais);
          return createdRepository;
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

  private void importStoresForCreatedRepository(Repository repository, TarArchiveInputStream tais) throws IOException {
    ArchiveEntry metadataEntry = tais.getNextEntry();
    if (metadataEntry.getName().equals(SCM_METADATA_TAR_FILE) && !metadataEntry.isDirectory()) {
      // Inside the repository tar archive stream is another tar archive.
      // The nested tar archive is wrapped in another TarArchiveInputStream inside the storeImporter
      storeImporter.importFromTarArchive(repository, tais);
    } else {
      throw new ImportFailedException(
        ContextEntry.ContextBuilder.entity(repository).build(),
        "Invalid import format. Missing metadata file 'scm-metadata.tar' in tar."
      );
    }
  }

  private Repository importRepositoryFromFile(Repository repository, TarArchiveInputStream tais) throws IOException {
    ArchiveEntry repositoryEntry = tais.getNextEntry();
    if (repositoryEntry.getName().endsWith(".dump") && !repositoryEntry.isDirectory()) {
      return repositoryManager.create(repository, repo -> {
        try (RepositoryService service = serviceFactory.create(repo)) {
          service.getUnbundleCommand().unbundle(new NoneClosingInputStream(tais));
        } catch (IOException e) {
          throw new ImportFailedException(
            ContextEntry.ContextBuilder.entity(repository).build(),
            "Repository import failed. Could not import repository from file.",
            e
          );
        }
      });
    } else {
      throw new ImportFailedException(
        ContextEntry.ContextBuilder.entity(repository).build(),
        "Invalid import format. Missing repository dump file."
      );
    }
  }

  private void checkScmEnvironment(Repository repository, TarArchiveInputStream tais) throws IOException {
    ArchiveEntry environmentEntry = tais.getNextEntry();
    if (environmentEntry.getName().equals(SCM_ENVIRONMENT_FILE) && !environmentEntry.isDirectory() && environmentEntry.getSize() < _1_MB) {
      boolean validEnvironment = compatibilityChecker.check(JAXB.unmarshal(new NoneClosingInputStream(tais), ScmEnvironment.class));
      if (!validEnvironment) {
        throw new ImportFailedException(
          ContextEntry.ContextBuilder.entity(repository).build(),
          "Incompatible SCM-Manager environment. Could not import file."
        );
      }
    } else {
      throw new ImportFailedException(
        ContextEntry.ContextBuilder.entity(repository).build(),
        "Invalid import format. Missing SCM-Manager environment description file 'scm-environment.xml' or file too big."
      );
    }
  }

  private void skipRepositoryMetadata(TarArchiveInputStream tais) throws IOException {
    tais.getNextEntry();
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
