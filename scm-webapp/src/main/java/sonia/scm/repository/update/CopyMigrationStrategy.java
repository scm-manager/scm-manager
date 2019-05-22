package sonia.scm.repository.update;

import sonia.scm.SCMContextProvider;
import sonia.scm.migration.UpdateException;
import sonia.scm.repository.AbstractSimpleRepositoryHandler;
import sonia.scm.repository.RepositoryLocationResolver;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

class CopyMigrationStrategy implements MigrationStrategy.Instance {

  private final SCMContextProvider contextProvider;
  private final RepositoryLocationResolver locationResolver;

  @Inject
  public CopyMigrationStrategy(SCMContextProvider contextProvider, RepositoryLocationResolver locationResolver) {
    this.contextProvider = contextProvider;
    this.locationResolver = locationResolver;
  }

  @Override
  public Path migrate(String id, String name, String type) {
    Path repositoryBasePath = locationResolver.forClass(Path.class).getLocation(id);
    Path targetDataPath = repositoryBasePath
      .resolve(AbstractSimpleRepositoryHandler.REPOSITORIES_NATIVE_DIRECTORY);
    Path sourceDataPath = getSourceDataPath(name, type);
    copyData(sourceDataPath, targetDataPath);
    return repositoryBasePath;
  }

  private Path getSourceDataPath(String name, String type) {
    return Arrays.stream(name.split("/"))
      .reduce(getTypeDependentPath(type), (path, namePart) -> path.resolve(namePart), (p1, p2) -> p1);
  }

  private Path getTypeDependentPath(String type) {
    return contextProvider.getBaseDirectory().toPath().resolve("repositories").resolve(type);
  }

  private void copyData(Path sourceDirectory, Path targetDirectory) {
    createDataDirectory(targetDirectory);
    Stream<Path> list = listSourceDirectory(sourceDirectory);
    list.forEach(
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

  private Stream<Path> listSourceDirectory(Path sourceDirectory) {
    try {
      return Files.list(sourceDirectory);
    } catch (IOException e) {
      throw new UpdateException("could not read original directory", e);
    }
  }

  private void copyFile(Path sourceFile, Path targetFile) {
    try {
      Files.copy(sourceFile, targetFile);
    } catch (IOException e) {
      throw new UpdateException("could not copy original file from " + sourceFile + " to " + targetFile, e);
    }
  }

  private void createDataDirectory(Path target) {
    try {
      Files.createDirectories(target);
    } catch (IOException e) {
      throw new UpdateException("could not create data directory " + target, e);
    }
  }
}
