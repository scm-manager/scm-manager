package sonia.scm.update.repository;

import sonia.scm.SCMContextProvider;
import sonia.scm.repository.RepositoryDirectoryHandler;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;

class InlineMigrationStrategy extends BaseMigrationStrategy {

  @Inject
  public InlineMigrationStrategy(SCMContextProvider contextProvider) {
    super(contextProvider);
  }

  @Override
  public Path migrate(String id, String name, String type) {
    Path repositoryBasePath = getSourceDataPath(name, type);
    Path targetDataPath = repositoryBasePath
      .resolve(RepositoryDirectoryHandler.REPOSITORIES_NATIVE_DIRECTORY);
    moveData(repositoryBasePath, targetDataPath);
    return repositoryBasePath;
  }

  private void moveData(Path sourceDirectory, Path targetDirectory) {
    createDataDirectory(targetDirectory);
    listSourceDirectory(sourceDirectory)
      .filter(sourceFile -> !targetDirectory.equals(sourceFile))
      .forEach(
        sourceFile -> {
          Path targetFile = targetDirectory.resolve(sourceFile.getFileName());
          if (Files.isDirectory(sourceFile)) {
            moveData(sourceFile, targetFile);
          } else {
            moveFile(sourceFile, targetFile);
          }
        }
      );
  }
}
