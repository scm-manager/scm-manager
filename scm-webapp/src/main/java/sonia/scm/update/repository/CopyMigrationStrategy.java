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
