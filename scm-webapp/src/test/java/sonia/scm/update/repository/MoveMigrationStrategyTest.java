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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContextProvider;
import sonia.scm.repository.RepositoryLocationResolver;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MoveMigrationStrategyTest {

  @Mock
  SCMContextProvider contextProvider;
  @Mock
  RepositoryLocationResolver locationResolver;
  @TempDir
  Path tempDir;

  @BeforeEach
  void mockContextProvider() {
    when(contextProvider.getBaseDirectory()).thenReturn(tempDir.toFile());
  }

  @BeforeEach
  void createV1Home() throws IOException {
    V1RepositoryFileSystem.createV1Home(tempDir);
  }

  @BeforeEach
  void mockLocationResolver() {
    RepositoryLocationResolver.RepositoryLocationResolverInstance instanceMock = mock(RepositoryLocationResolver.RepositoryLocationResolverInstance.class);
    when(locationResolver.forClass(Path.class)).thenReturn(instanceMock);
    when(instanceMock.createLocation(anyString())).thenAnswer(invocation -> tempDir.resolve((String) invocation.getArgument(0)));
  }

  @Test
  void shouldUseStandardDirectory() {
    Path target = new MoveMigrationStrategy(contextProvider, locationResolver).migrate("b4f-a9f0-49f7-ad1f-37d3aae1c55f", "some/more/directories/than/one", "git").get();
    assertThat(target).isEqualTo(tempDir.resolve("b4f-a9f0-49f7-ad1f-37d3aae1c55f"));
  }

  @Test
  void shouldMoveDataDirectory() {
    Path target = new MoveMigrationStrategy(contextProvider, locationResolver).migrate("b4f-a9f0-49f7-ad1f-37d3aae1c55f", "some/more/directories/than/one", "git").get();
    assertThat(target.resolve("data")).exists();
    Path originalDataDir = tempDir
      .resolve("repositories")
      .resolve("git")
      .resolve("some");
    assertThat(originalDataDir).doesNotExist();
  }
}
