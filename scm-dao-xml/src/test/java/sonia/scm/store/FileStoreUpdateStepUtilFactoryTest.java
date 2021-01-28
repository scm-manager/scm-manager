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
