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

package sonia.scm.update.repository;

import sonia.scm.SCMContextProvider;
import sonia.scm.migration.UpdateException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Stream;

abstract class BaseMigrationStrategy implements MigrationStrategy.Instance {

  private final V1RepositoryMigrationLocationResolver locationResolver;

  BaseMigrationStrategy(SCMContextProvider contextProvider) {
    this.locationResolver = new V1RepositoryMigrationLocationResolver(contextProvider);
  }

  Path getSourceDataPath(String name, String type) {
    return Arrays.stream(name.split("/"))
      .reduce(getTypeDependentPath(type), (path, namePart) -> path.resolve(namePart), (p1, p2) -> p1);
  }

  Path getTypeDependentPath(String type) {
    return locationResolver.getTypeDependentPath(type);
  }

  void listSourceDirectory(Path sourceDirectory, Consumer<Stream<Path>> pathConsumer) {
    try (Stream<Path> paths = Files.list(sourceDirectory)) {
      pathConsumer.accept(paths);
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
