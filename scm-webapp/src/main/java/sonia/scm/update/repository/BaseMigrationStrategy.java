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
