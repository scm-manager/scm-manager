package sonia.scm.update.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContextProvider;
import sonia.scm.repository.RepositoryDirectoryHandler;
import sonia.scm.repository.RepositoryLocationResolver;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static java.util.Optional.of;

class InlineMigrationStrategy extends BaseMigrationStrategy {

  private static final Logger LOG = LoggerFactory.getLogger(InlineMigrationStrategy.class);

  private final RepositoryLocationResolver locationResolver;

  @Inject
  public InlineMigrationStrategy(SCMContextProvider contextProvider, RepositoryLocationResolver locationResolver) {
    super(contextProvider);
    this.locationResolver = locationResolver;
  }

  @Override
  public Optional<Path> migrate(String id, String name, String type) {
    Path repositoryBasePath = getSourceDataPath(name, type);
    locationResolver.forClass(Path.class).setLocation(id, repositoryBasePath);
    Path targetDataPath = repositoryBasePath
      .resolve(RepositoryDirectoryHandler.REPOSITORIES_NATIVE_DIRECTORY);
    LOG.info("moving repository data from {} to {}", repositoryBasePath, targetDataPath);
    moveData(repositoryBasePath, targetDataPath);
    return of(repositoryBasePath);
  }

  private void moveData(Path sourceDirectory, Path targetDirectory) {
    moveData(sourceDirectory, targetDirectory, false);
  }

  private void moveData(Path sourceDirectory, Path targetDirectory, boolean deleteDirectory) {
    createDataDirectory(targetDirectory);
    listSourceDirectory(sourceDirectory)
      .filter(sourceFile -> !targetDirectory.equals(sourceFile))
      .forEach(
        sourceFile -> {
          Path targetFile = targetDirectory.resolve(sourceFile.getFileName());
          if (Files.isDirectory(sourceFile)) {
            moveData(sourceFile, targetFile, true);
          } else {
            moveFile(sourceFile, targetFile);
          }
        }
      );
    if (deleteDirectory) {
      try {
        Files.delete(sourceDirectory);
      } catch (IOException e) {
        LOG.warn("could not delete source repository directory {}", sourceDirectory);
      }
    }
  }
}
