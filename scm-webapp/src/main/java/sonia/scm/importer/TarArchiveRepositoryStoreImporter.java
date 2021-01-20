package sonia.scm.importer;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import sonia.scm.ContextEntry;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.ImportFailedException;
import sonia.scm.store.RepositoryStoreImporter;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;

public class TarArchiveRepositoryStoreImporter {

  private final RepositoryStoreImporter repositoryStoreImporter;

  @Inject
  public TarArchiveRepositoryStoreImporter(RepositoryStoreImporter repositoryStoreImporter) {
    this.repositoryStoreImporter = repositoryStoreImporter;
  }

  public void importFromTarArchive(Repository repository, InputStream inputStream) {
    try (TarArchiveInputStream tais = new TarArchiveInputStream(inputStream)) {
      ArchiveEntry entry = tais.getNextEntry();
      while (entry != null) {
        String[] entryPathParts = entry.getName().split("/");
        if (!isValidStorePath(entryPathParts)) {
          throw new ImportFailedException(ContextEntry.ContextBuilder.entity(repository).build(), "Invalid store path in metadata file");
        }
        if (entryPathParts[1].equals("data")) {
          repositoryStoreImporter.doImport(repository).importStore(entryPathParts[1], entryPathParts[2]).importEntry(entryPathParts[3], tais);
        } else {
          repositoryStoreImporter.doImport(repository).importStore(entryPathParts[1], "").importEntry(entryPathParts[2], tais);
        }
        entry = tais.getNextEntry();
      }
    } catch (IOException e) {
      throw  new ImportFailedException(ContextEntry.ContextBuilder.entity(repository).build(), "Could not import stores from metadata file.", e);
    }
  }

  private boolean isValidStorePath(String[] entryPathParts) {
    if (entryPathParts.length < 3 || entryPathParts.length > 4) {
      return false;
    }
    if (entryPathParts[1].equals("data")) {
      return entryPathParts.length == 4;
    }
    if (entryPathParts[1].equals("config")) {
      return entryPathParts.length == 3;
    }
    // We only support config and data stores yet
    return false;
  }
}
