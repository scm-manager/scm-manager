package sonia.scm.repository.update;

import sonia.scm.SCMContextProvider;
import sonia.scm.migration.UpdateException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

abstract class BaseMigrationStrategy implements MigrationStrategy.Instance {

  private final SCMContextProvider contextProvider;

  BaseMigrationStrategy(SCMContextProvider contextProvider) {
    this.contextProvider = contextProvider;
  }

  Path getSourceDataPath(String name, String type) {
    return Arrays.stream(name.split("/"))
      .reduce(getTypeDependentPath(type), (path, namePart) -> path.resolve(namePart), (p1, p2) -> p1);
  }

  Path getTypeDependentPath(String type) {
    return contextProvider.getBaseDirectory().toPath().resolve("repositories").resolve(type);
  }

  Stream<Path> listSourceDirectory(Path sourceDirectory) {
    try {
      return Files.list(sourceDirectory);
    } catch (IOException e) {
      throw new UpdateException("could not read original directory", e);
    }
  }

  void createDataDirectory(Path target) {
    try {
      Files.createDirectories(target);
    } catch (IOException e) {
      throw new UpdateException("could not create data directory " + target, e);
    }
  }

  void moveFile(Path sourceFile, Path targetFile) {
    try {
      Files.move(sourceFile, targetFile);
    } catch (IOException e) {
      throw new UpdateException("could not move data file from " + sourceFile + " to " + targetFile, e);
    }
  }

  void copyFile(Path sourceFile, Path targetFile) {
    try {
      Files.copy(sourceFile, targetFile);
    } catch (IOException e) {
      throw new UpdateException("could not copy original file from " + sourceFile + " to " + targetFile, e);
    }
  }
}
