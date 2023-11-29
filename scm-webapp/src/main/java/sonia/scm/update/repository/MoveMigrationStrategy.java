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

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContextProvider;
import sonia.scm.repository.RepositoryDirectoryHandler;
import sonia.scm.repository.RepositoryLocationResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Optional.of;

class MoveMigrationStrategy extends BaseMigrationStrategy {

  private static final Logger LOG = LoggerFactory.getLogger(MoveMigrationStrategy.class);

  private final RepositoryLocationResolver locationResolver;

  @Inject
  public MoveMigrationStrategy(SCMContextProvider contextProvider, RepositoryLocationResolver locationResolver) {
    super(contextProvider);
    this.locationResolver = locationResolver;
  }

  @Override
  public Optional<Path> migrate(String id, String name, String type) {
    Path repositoryBasePath = locationResolver.forClass(Path.class).createLocation(id);
    Path targetDataPath = repositoryBasePath
      .resolve(RepositoryDirectoryHandler.REPOSITORIES_NATIVE_DIRECTORY);
    Path sourceDataPath = getSourceDataPath(name, type);
    LOG.info("moving repository data from {} to {}", sourceDataPath, targetDataPath);
    moveData(sourceDataPath, targetDataPath);
    deleteOldDataDir(getTypeDependentPath(type), name);
    return of(repositoryBasePath);
  }

  private void deleteOldDataDir(Path rootPath, String name) {
    delete(rootPath, asList(name.split("/")));
  }

  private void delete(Path rootPath, List<String> directories) {
    if (directories.isEmpty()) {
      return;
    }
    Path directory = rootPath.resolve(directories.get(0));
    delete(directory, directories.subList(1, directories.size()));
    try {
      Files.deleteIfExists(directory);
    } catch (IOException e) {
      LOG.warn("could not delete source repository directory {}", directory);
    }
  }

  private void moveData(Path sourceDirectory, Path targetDirectory) {
    createDataDirectory(targetDirectory);
    listSourceDirectory(sourceDirectory, paths -> paths.forEach(
      sourceFile -> {
        Path targetFile = targetDirectory.resolve(sourceFile.getFileName());
        if (Files.isDirectory(sourceFile)) {
          LOG.trace("traversing down into sub directory {}", sourceFile);
          moveData(sourceFile, targetFile);
        } else {
          LOG.trace("moving file {} to {}", sourceFile, targetFile);
          moveFile(sourceFile, targetFile);
        }
      }
    ));
    try {
      LOG.trace("deleting source directory {}", sourceDirectory);
      Files.delete(sourceDirectory);
    } catch (IOException e) {
      LOG.warn("could not delete source repository directory {}", sourceDirectory);
    }
  }
}
