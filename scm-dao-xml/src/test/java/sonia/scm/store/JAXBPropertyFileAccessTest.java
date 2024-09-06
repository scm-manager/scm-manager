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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContextProvider;
import sonia.scm.io.DefaultFileSystem;
import sonia.scm.repository.InitialRepositoryLocationResolver;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.repository.xml.PathBasedRepositoryLocationResolver;
import sonia.scm.update.PropertyFileAccess;
import sonia.scm.util.IOUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class JAXBPropertyFileAccessTest {

  public static final String REPOSITORY_ID = "repoId";
  public static final String STORE_NAME = "test";

  @Mock
  SCMContextProvider contextProvider;

  RepositoryLocationResolver locationResolver;

  JAXBPropertyFileAccess fileAccess;

  @TempDir
  private Path tempDir;

  @BeforeEach
  void initTempDir() {
    lenient().when(contextProvider.getBaseDirectory()).thenReturn(tempDir.toFile());
    lenient().when(contextProvider.resolve(any())).thenAnswer(invocation -> tempDir.resolve(invocation.getArgument(0).toString()));

    locationResolver = new PathBasedRepositoryLocationResolver(contextProvider, new InitialRepositoryLocationResolver(emptySet()), new DefaultFileSystem());

    fileAccess = new JAXBPropertyFileAccess(contextProvider, locationResolver);
  }

  @Test
  void shouldRenameGlobalConfigFile() throws IOException {
    Path baseDirectory = contextProvider.getBaseDirectory().toPath();
    Path configDirectory = baseDirectory.resolve(StoreConstants.CONFIG_DIRECTORY_NAME);

    Files.createDirectories(configDirectory);

    Path oldPath = configDirectory.resolve("old" + StoreConstants.FILE_EXTENSION);
    Files.createFile(oldPath);

    fileAccess.renameGlobalConfigurationFrom("old").to("new");

    Path newPath = configDirectory.resolve("new" + StoreConstants.FILE_EXTENSION);
    assertThat(oldPath).doesNotExist();
    assertThat(newPath).exists();
  }

  @Nested
  class ForExistingRepository {


    @BeforeEach
    void createRepositoryLocation() {
      locationResolver.forClass(Path.class).createLocation(REPOSITORY_ID);
    }

    @Test
    void shouldMoveStoreFileToRepositoryBasedLocation() throws IOException {
      createV1StoreFile("myStore.xml");

      fileAccess.forStoreName(STORE_NAME).moveAsRepositoryStore(Paths.get("myStore.xml"), REPOSITORY_ID);

      assertThat(tempDir.resolve("repositories").resolve(REPOSITORY_ID).resolve("store").resolve("data").resolve(STORE_NAME).resolve("myStore.xml")).exists();
    }

    @Test
    void shouldMoveAllStoreFilesToRepositoryBasedLocations() throws IOException {
      locationResolver.forClass(Path.class).createLocation("repoId2");

      createV1StoreFile(REPOSITORY_ID + ".xml");
      createV1StoreFile("repoId2.xml");

      PropertyFileAccess.StoreFileTools statisticStoreAccess = fileAccess.forStoreName(STORE_NAME);
      statisticStoreAccess.forStoreFiles(statisticStoreAccess::moveAsRepositoryStore);

      assertThat(tempDir.resolve("repositories").resolve(REPOSITORY_ID).resolve("store").resolve("data").resolve(STORE_NAME).resolve("repoId.xml")).exists();
      assertThat(tempDir.resolve("repositories").resolve("repoId2").resolve("store").resolve("data").resolve(STORE_NAME).resolve("repoId2.xml")).exists();
    }
  }

  private void createV1StoreFile(String name) throws IOException {
    Path v1Dir = tempDir.resolve("var").resolve("data").resolve(STORE_NAME);
    IOUtil.mkdirs(v1Dir.toFile());
    Files.createFile(v1Dir.resolve(name));
  }

  @Nested
  class ForMissingRepository {

    @Test
    void shouldIgnoreStoreFile() throws IOException {
      createV1StoreFile("myStore.xml");

      fileAccess.forStoreName(STORE_NAME).moveAsRepositoryStore(Paths.get("myStore.xml"), REPOSITORY_ID);

      assertThat(tempDir.resolve("repositories").resolve(REPOSITORY_ID).resolve("store").resolve("data").resolve(STORE_NAME).resolve("myStore.xml")).doesNotExist();
    }
  }
}
