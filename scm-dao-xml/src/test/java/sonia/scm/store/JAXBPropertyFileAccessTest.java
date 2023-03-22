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

    locationResolver = new PathBasedRepositoryLocationResolver(contextProvider, new InitialRepositoryLocationResolver(), new DefaultFileSystem());

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
