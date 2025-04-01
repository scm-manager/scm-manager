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
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.repository.api.ImportFailedException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.importexport.FullScmRepositoryExporter.QUERYABLE_STORE_DATA_FILE_NAME;

@Slf4j
class QueryableStoreImportStep implements ImportStep {
  private final RepositoryQueryableStoreExporter queryableStoreExporter;
  private final RepositoryLocationResolver locationResolver;

  @Inject
  QueryableStoreImportStep(RepositoryQueryableStoreExporter queryableStoreExporter, RepositoryLocationResolver locationResolver) {
    this.queryableStoreExporter = queryableStoreExporter;
    this.locationResolver = locationResolver;
  }

  @Override
  public boolean handle(TarArchiveEntry entry, ImportState state, InputStream inputStream) {
    if (entry.getName().equals(QUERYABLE_STORE_DATA_FILE_NAME) && !entry.isDirectory()) {
      log.trace("Importing store from tar");
      state.getLogger().step("importing queryable stores");

      Path repositoryPath = locationResolver
        .forClass(Path.class)
        .getLocation(state.getRepository().getId());

      try {
        extractTarToDirectory(inputStream, repositoryPath.toFile());
        queryableStoreExporter.importStores(state.getRepository().getId(), repositoryPath.toFile());

        return true;
      } catch (IOException e) {
        throw new ImportFailedException(entity(state.getRepository()).build(), "Failed to extract TAR content", e);
      }
    }
    return false;
  }

  private void extractTarToDirectory(InputStream inputStream, File outputDir) throws IOException {
    try (TarArchiveInputStream tarInput = new NoneClosingTarArchiveInputStream(inputStream)) {
      TarArchiveEntry entry;
      while ((entry = tarInput.getNextEntry()) != null) {
        if (entry.isDirectory()) {
          continue;
        }
        File outputFile = new File(outputDir, entry.getName());
        outputFile.getParentFile().mkdirs();

        try (OutputStream outputStream = Files.newOutputStream(outputFile.toPath())) {
          tarInput.transferTo(outputStream);
        }
      }
    }
  }
}
