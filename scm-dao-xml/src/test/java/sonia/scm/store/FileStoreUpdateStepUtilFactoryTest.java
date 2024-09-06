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

package sonia.scm.store;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContextProvider;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.update.StoreUpdateStepUtilFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class FileStoreUpdateStepUtilFactoryTest {

  @Mock
  private RepositoryLocationResolver locationResolver;
  @Mock
  private RepositoryLocationResolver.RepositoryLocationResolverInstance locationResolverInstance;
  @Mock
  private SCMContextProvider contextProvider;

  @InjectMocks
  private FileStoreUpdateStepUtilFactory factory;

  private Path globalPath;
  private Path repositoryPath;

  @BeforeEach
  void initPaths(@TempDir Path temp) throws IOException {
    globalPath = temp.resolve("global");
    Files.createDirectories(globalPath);
    lenient().doReturn(globalPath.toFile()).when(contextProvider).getBaseDirectory();

    repositoryPath = temp.resolve("repo");
    Files.createDirectories(repositoryPath);
    lenient().doReturn(true).when(locationResolver).supportsLocationType(Path.class);
    lenient().doReturn(locationResolverInstance).when(locationResolver).forClass(Path.class);
    lenient().doReturn(repositoryPath).when(locationResolverInstance).getLocation("repo-id");
  }

  @Test
  void shouldMoveGlobalDataDirectory() throws IOException {
    Path dataPath = globalPath.resolve("var").resolve("data");
    Files.createDirectories(dataPath.resolve("something"));
    Files.createFile(dataPath.resolve("something").resolve("some.file"));
    StoreUpdateStepUtilFactory.StoreUpdateStepUtil util =
      factory
        .forType(StoreType.DATA)
        .forName("something")
        .build();

    util.renameStore("new-name");

    assertThat(dataPath.resolve("new-name").resolve("some.file")).exists();
    assertThat(dataPath.resolve("something")).doesNotExist();
  }

  @Test
  void shouldMoveRepositoryDataDirectory() throws IOException {
    Path dataPath = repositoryPath.resolve("store").resolve("data");
    Files.createDirectories(dataPath.resolve("something"));
    Files.createFile(dataPath.resolve("something").resolve("some.file"));
    StoreUpdateStepUtilFactory.StoreUpdateStepUtil util =
      factory
        .forType(StoreType.DATA)
        .forName("something")
        .forRepository("repo-id")
        .build();

    util.renameStore("new-name");

    assertThat(dataPath.resolve("new-name").resolve("some.file")).exists();
    assertThat(dataPath.resolve("something")).doesNotExist();
  }

  @Test
  void shouldHandleMissingMoveGlobalDataDirectory() throws IOException {
    StoreUpdateStepUtilFactory.StoreUpdateStepUtil util =
      factory
        .forType(StoreType.DATA)
        .forName("something")
        .build();

    util.renameStore("new-name");

    assertThat(globalPath.resolve("new-name")).doesNotExist();
  }
}
