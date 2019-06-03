package sonia.scm.repository.update;

import sonia.scm.SCMContextProvider;
import sonia.scm.repository.RepositoryDirectoryHandler;
import sonia.scm.repository.RepositoryLocationResolver;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;

class CopyMigrationStrategy extends BaseMigrationStrategy {

  private final RepositoryLocationResolver locationResolver;

  @Inject
  public CopyMigrationStrategy(SCMContextProvider contextProvider, RepositoryLocationResolver locationResolver) {
    super(contextProvider);
    this.locationResolver = locationResolver;
  }

  @Override
  public Path migrate(String id, String name, String type) {
    Path repositoryBasePath = locationResolver.forClass(Path.class).getLocation(id);
    Path targetDataPath = repositoryBasePath
      .resolve(RepositoryDirectoryHandler.REPOSITORIES_NATIVE_DIRECTORY);
    Path sourceDataPath = getSourceDataPath(name, type);
    copyData(sourceDataPath, targetDataPath);
    return repositoryBasePath;
  }

  private void copyData(Path sourceDirectory, Path targetDirectory) {
    createDataDirectory(targetDirectory);
    listSourceDirectory(sourceDirectory).forEach(
      sourceFile -> {
        Path targetFile = targetDirectory.resolve(sourceFile.getFileName());
        if (Files.isDirectory(sourceFile)) {
          copyData(sourceFile, targetFile);
        } else {
          copyFile(sourceFile, targetFile);
        }
      }
    );
  }
}
