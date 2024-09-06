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
