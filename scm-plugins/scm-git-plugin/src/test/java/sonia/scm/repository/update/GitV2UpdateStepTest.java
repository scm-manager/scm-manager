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
