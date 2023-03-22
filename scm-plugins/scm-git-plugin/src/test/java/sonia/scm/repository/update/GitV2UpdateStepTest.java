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

package sonia.scm.repository.update;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.update.UpdateStepRepositoryMetadataAccess;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitV2UpdateStepTest {

  @Mock
  RepositoryLocationResolver locationResolver;
  @Mock
  RepositoryLocationResolver.RepositoryLocationResolverInstance<Path> locationResolverInstance;
  @Mock
  UpdateStepRepositoryMetadataAccess<Path> repositoryMetadataAccess;

  @InjectMocks
  GitV2UpdateStep updateStep;

  @TempDir
  Path temp;

  @BeforeEach
  void createDataDirectory() throws IOException {
    Files.createDirectories(temp.resolve("data"));
  }

  @BeforeEach
  void initRepositoryFolder() {
    when(locationResolver.forClass(Path.class)).thenReturn(locationResolverInstance);
    when(repositoryMetadataAccess.read(temp)).thenReturn(new Repository("123", "git", "space", "X"));
    doAnswer(invocation -> {
      invocation.getArgument(0, BiConsumer.class).accept("123", temp);
      return null;
    }).when(locationResolverInstance).forAllLocations(any());
  }

  @Test
  void shouldWriteConfigFileForBareRepositories() {
    updateStep.doUpdate();

    assertThat(temp.resolve("data").resolve("config")).exists();
  }

  @Test
  void shouldWriteConfigFileForNonBareRepositories() throws IOException {
    Files.createDirectories(temp.resolve("data").resolve(".git"));

    updateStep.doUpdate();

    assertThat(temp.resolve("data").resolve("config")).doesNotExist();
    assertThat(temp.resolve("data").resolve(".git").resolve("config")).exists();
  }
}
