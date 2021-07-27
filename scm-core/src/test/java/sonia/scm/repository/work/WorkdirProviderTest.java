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

package sonia.scm.repository.work;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.repository.RepositoryLocationResolver.RepositoryLocationResolverInstance;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkdirProviderTest {

  @Mock
  private RepositoryLocationResolver repositoryLocationResolver;
  @Mock
  private RepositoryLocationResolverInstance<Path> repositoryLocationResolverInstance;

  @BeforeEach
  void initResolver() {
    lenient().when(repositoryLocationResolver.forClass(Path.class)).thenReturn(repositoryLocationResolverInstance);
  }

  @Test
  void shouldUseGlobalTempDirectory(@TempDir Path temp) {
    WorkdirProvider provider = new WorkdirProvider(temp.toFile(), repositoryLocationResolver, true);

    File newWorkdir = provider.createNewWorkdir();

    assertThat(newWorkdir)
      .exists()
      .hasParent(temp.toFile());
    verify(repositoryLocationResolverInstance, never()).getLocation(anyString());
  }

  @Test
  void shouldUseRepositorySpecificDirectory(@TempDir Path temp) {
    when(repositoryLocationResolverInstance.getLocation("42")).thenReturn(temp.resolve("repo-dir"));

    WorkdirProvider provider = new WorkdirProvider(temp.toFile(), repositoryLocationResolver, true);

    File newWorkdir = provider.createNewWorkdir("42");

    assertThat(newWorkdir).exists();
    assertThat(newWorkdir.getParentFile()).hasName("work");
    assertThat(newWorkdir.getParentFile().getParentFile()).hasName("repo-dir");
  }

  @Test
  void shouldUseGlobalDirectoryIfExplicitlySet(@TempDir Path temp) {
    WorkdirProvider provider = new WorkdirProvider(temp.toFile(), repositoryLocationResolver, false);

    File newWorkdir = provider.createNewWorkdir("42");

    assertThat(newWorkdir)
      .exists()
      .hasParent(temp.toFile());
    verify(repositoryLocationResolverInstance, never()).getLocation(anyString());
  }

  @Nested
  class WithExistingGlobalWorkDir {

    private Path globalRootDir;
    private WorkdirProvider provider;

    @BeforeEach
    void createExistingWorkDir(@TempDir Path temp) throws IOException {
      globalRootDir = temp.resolve("global");
      Files.createDirectories(globalRootDir.resolve("global-temp"));

      provider = new WorkdirProvider(globalRootDir.toFile(), repositoryLocationResolver, true);
    }

    @Test
    void shouldDeleteOldGlobalWorkDirsOnStartup() {
      provider.contextInitialized(null);

      assertThat(globalRootDir).isEmptyDirectory();
    }

    @Test
    void shouldDeleteOldGlobalWorkDirsOnShutdown() {
      provider.contextDestroyed(null);

      assertThat(globalRootDir).isEmptyDirectory();
    }
  }

  @Nested
  class WithExistingRepositoryWorkDir {

    private Path repositoryRootDir;
    private WorkdirProvider provider;

    @BeforeEach
    void createExistingWorkDir(@TempDir Path temp) throws IOException {
      repositoryRootDir = temp.resolve("42");
      Files.createDirectories(repositoryRootDir.resolve("work").resolve("repo-temp"));

      doAnswer(
        invocationOnMock -> {
          invocationOnMock.getArgument(0, BiConsumer.class)
            .accept("42", repositoryRootDir);
          return null;
        }
      ).when(repositoryLocationResolverInstance).forAllLocations(any());
      provider = new WorkdirProvider(temp.resolve("global").toFile(), repositoryLocationResolver, true);
    }

    @Test
    void shouldDeleteOldRepositoryRelatedWorkDirsOnStartup() {
      provider.contextInitialized(null);

      assertThat(repositoryRootDir.resolve("work")).isEmptyDirectory();
    }

    @Test
    void shouldDeleteOldRepositoryRelatedWorkDirsOnShutdown() {
      provider.contextInitialized(null);

      assertThat(repositoryRootDir.resolve("work")).isEmptyDirectory();
    }
  }
}
