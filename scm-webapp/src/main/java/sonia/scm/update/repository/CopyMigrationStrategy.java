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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static java.util.Optional.of;

class CopyMigrationStrategy extends BaseMigrationStrategy {

  private static final Logger LOG = LoggerFactory.getLogger(CopyMigrationStrategy.class);

  private final RepositoryLocationResolver locationResolver;

  @Inject
  public CopyMigrationStrategy(SCMContextProvider contextProvider, RepositoryLocationResolver locationResolver) {
    super(contextProvider);
    this.locationResolver = locationResolver;
  }

  @Override
  public Optional<Path> migrate(String id, String name, String type) {
    Path repositoryBasePath = locationResolver.forClass(Path.class).createLocation(id);
    Path targetDataPath = repositoryBasePath
      .resolve(RepositoryDirectoryHandler.REPOSITORIES_NATIVE_DIRECTORY);
    Path sourceDataPath = getSourceDataPath(name, type);
    LOG.info("copying repository data from {} to {}", sourceDataPath, targetDataPath);
    copyData(sourceDataPath, targetDataPath);
    return of(repositoryBasePath);
  }

  private void copyData(Path sourceDirectory, Path targetDirectory) {
    createDataDirectory(targetDirectory);
    listSourceDirectory(sourceDirectory, paths -> paths.forEach(
      sourceFile -> {
        Path targetFile = targetDirectory.resolve(sourceFile.getFileName());
        if (Files.isDirectory(sourceFile)) {
          LOG.trace("traversing down into sub directory {}", sourceFile);
          copyData(sourceFile, targetFile);
        } else {
          LOG.trace("copying file {} to {}", sourceFile, targetFile);
          copyFile(sourceFile, targetFile);
        }
      }
    ));
  }
}
