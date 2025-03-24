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

package sonia.scm.repository.update;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.TempDirRepositoryLocationResolver;
import sonia.scm.migration.RepositoryUpdateContext;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.update.UpdateStepRepositoryMetadataAccess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.io.CleanupMode.ALWAYS;

@ExtendWith(MockitoExtension.class)
class UpdatePackFilterUpdateStepTest {

  RepositoryLocationResolver repositoryLocationResolver;

  UpdateStepRepositoryMetadataAccess<Path> updateStepRepositoryMetadataAccess;

  @Nested
  class DoUpdate {

    @TempDir(cleanup = ALWAYS)
    Path tempDir;

    UpdatePackFilterUpdateStep target;

    private static final String EXAMPLE_REPOSITORY_ID = "3ZUZMNJn3E";

    @BeforeEach
    void setUp() throws IOException {
      String sourcePath = "src/test/resources/scm-home/repositories/exampleGitRepoWithoutFilterUpdate";
      loadFilesIntoTempDir(tempDir, sourcePath);

      repositoryLocationResolver = new TempDirRepositoryLocationResolver(tempDir.toFile());
      updateStepRepositoryMetadataAccess = location -> {
        Repository repository = RepositoryTestData.createRestaurantAtTheEndOfTheUniverse("git");
        repository.setId(EXAMPLE_REPOSITORY_ID);
        return repository;
      };
    }

    @Test
    void shouldWriteAllowFilterLineWithinConfig() throws Exception {
      target = new UpdatePackFilterUpdateStep(repositoryLocationResolver, updateStepRepositoryMetadataAccess);

      target.doUpdate(new RepositoryUpdateContext(EXAMPLE_REPOSITORY_ID));

      File configFile = tempDir.resolve("data/config").toFile();

      boolean containsAllowFilter = false;
      try (BufferedReader br = new BufferedReader(new FileReader(configFile.getAbsolutePath()))) {
        do {
          String line = br.readLine();
          containsAllowFilter |= line.contains("allowFilter") && line.contains("true");
        } while (br.readLine() != null);
      }
      assertTrue(containsAllowFilter);
    }
  }

  private void loadFilesIntoTempDir(Path tempDir, String sourcePath) throws IOException {
    File repositorySourcePath = new File(sourcePath);
    try (Stream<Path> sources = Files.walk(repositorySourcePath.toPath())) {
      sources.forEach(source -> {
        Path destination = Paths.get(tempDir.toString(), source.toString().substring(repositorySourcePath.toString().length()));
        if (!destination.toFile().exists()) {
          try {
            Files.copy(source, destination);
          } catch (IOException e) {
            fail("An exception occurred during temporary repository file setup.", e);
          }
        }
      });
    }
  }
}
