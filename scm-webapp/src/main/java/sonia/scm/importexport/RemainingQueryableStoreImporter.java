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

package sonia.scm.importexport;

import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.repository.xml.PathBasedRepositoryLocationResolver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The job of this class is to check for remaining queryable store data from former imports, that have not been
 * imported yet. This can happen if a repository was imported, when not all plugins were installed but those plugins
 * are installed now. After this is done, the update process has to be run for all repositories.
 */
@Slf4j
public class RemainingQueryableStoreImporter {

  private final RepositoryLocationResolver.RepositoryLocationResolverInstance<Path> repositoryLocationResolverInstance;
  private final RepositoryQueryableStoreExporter queryableStoreExporter;

  @Inject
  public RemainingQueryableStoreImporter(PathBasedRepositoryLocationResolver repositoryLocationResolver,
                                         RepositoryQueryableStoreExporter queryableStoreExporter) {
    this.repositoryLocationResolverInstance = repositoryLocationResolver.create(Path.class);
    this.queryableStoreExporter = queryableStoreExporter;
  }

  public void onInitializationCompleted() {
    log.info("Starting import of remaining queryable store data for all repositories.");

    repositoryLocationResolverInstance.forAllLocations((repositoryId, repositoryPath) -> {
      File repoDir = repositoryPath.toFile();
      File dataDir = new File(repoDir, "queryable-store-data");

      if (dataDir.exists() && dataDir.isDirectory()) {
        List<File> xmlFiles = getXmlFiles(dataDir);
        if (!xmlFiles.isEmpty()) {
          log.info("Found {} XML files in repository {} - importing...", xmlFiles.size(), repositoryId);
          queryableStoreExporter.importStores(repositoryId, repoDir);
        }
      }
    });

    log.info("Finished importing queryable store data.");
  }

  private List<File> getXmlFiles(File directory) {
    try (Stream<Path> files = Files.list(directory.toPath())) {
      return files
        .map(Path::toFile)
        .filter(file -> file.getName().endsWith(".xml"))
        .collect(Collectors.toList());
    } catch (IOException e) {
      log.error("Error reading directory {}: {}", directory.getAbsolutePath(), e.getMessage());
      return List.of();
    }
  }
}

