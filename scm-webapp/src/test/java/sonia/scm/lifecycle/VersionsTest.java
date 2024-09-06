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

package sonia.scm.lifecycle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContextProvider;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith({MockitoExtension.class})
class VersionsTest {

  @Mock
  private SCMContextProvider contextProvider;

  @InjectMocks
  private Versions versions;

  @Test
  void shouldReturnTrueForVersionsPreviousTo160(@TempDir Path directory) throws IOException {
    setVersion(directory, "1.59");
    assertThat(versions.isPreviousVersionTooOld()).isTrue();

    setVersion(directory, "1.12");
    assertThat(versions.isPreviousVersionTooOld()).isTrue();
  }

  @Test
  void shouldReturnFalseForVersion160(@TempDir Path directory) throws IOException {
    setVersion(directory, "1.60");
    assertThat(versions.isPreviousVersionTooOld()).isFalse();
  }

  @Test
  void shouldNotFailIfVersionContainsLineBreak(@TempDir Path directory) throws IOException {
    setVersion(directory, "1.59\n");
    assertThat(versions.isPreviousVersionTooOld()).isTrue();
  }

  @Test
  void shouldReturnFalseForVersionsNewerAs160(@TempDir Path directory) throws IOException {
    setVersion(directory, "1.61");
    assertThat(versions.isPreviousVersionTooOld()).isFalse();

    setVersion(directory, "1.82");
    assertThat(versions.isPreviousVersionTooOld()).isFalse();
  }

  @Test
  void shouldReturnFalseForNonExistingVersionFile(@TempDir Path directory) {
    setVersionFile(directory.resolve("version.txt"));
    assertThat(versions.isPreviousVersionTooOld()).isFalse();
  }

  @Test
  void shouldWriteNewVersion(@TempDir Path directory) {
    Path config = directory.resolve("config");
    doReturn(config).when(contextProvider).resolve(Paths.get("config"));
    doReturn("2.0.0").when(contextProvider).getVersion();

    versions.writeNewVersion();

    Path versionFile = config.resolve("version.txt");
    assertThat(versionFile).exists().hasContent("2.0.0");
  }

  private void setVersion(Path directory, String version) throws IOException {
    Path file = directory.resolve("version.txt");
    Files.write(file, version.getBytes(StandardCharsets.UTF_8));
    setVersionFile(file);
  }

  private void setVersionFile(Path file) {
    doReturn(file).when(contextProvider).resolve(Paths.get("config", "version.txt"));
  }
}
