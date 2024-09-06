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

package sonia.scm.plugin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PluginArchiveCleanerTest {
  private final PluginArchiveCleaner cleaner = new PluginArchiveCleaner();

  @Test
  void shouldDoNothingBecauseThresholdIsNotReached(@TempDir Path tmp) throws IOException {
    Files.createDirectories(tmp.resolve("2023-09-01"));
    Files.createDirectories(tmp.resolve("2023-09-02"));
    Files.createDirectories(tmp.resolve("2023-09-03"));
    Files.createDirectories(tmp.resolve("2023-09-04"));

    cleaner.cleanup(tmp);

    assertThat(tmp.resolve("2023-09-01")).exists();
    assertThat(tmp.resolve("2023-09-02")).exists();
    assertThat(tmp.resolve("2023-09-03")).exists();
    assertThat(tmp.resolve("2023-09-04")).exists();
  }

  @Test
  void shouldDeleteOldestFolder(@TempDir Path tmp) throws IOException {
    Files.createDirectories(tmp.resolve("2023-09-01"));
    Files.createDirectories(tmp.resolve("2023-09-02"));
    Files.createDirectories(tmp.resolve("2023-09-03"));
    Files.createDirectories(tmp.resolve("2023-09-04"));
    Files.createDirectories(tmp.resolve("2023-09-05"));
    Files.createDirectories(tmp.resolve("2023-09-06"));

    cleaner.cleanup(tmp);

    assertThat(tmp.resolve("2023-09-03")).exists();
    assertThat(tmp.resolve("2023-09-04")).exists();
    assertThat(tmp.resolve("2023-09-05")).exists();
    assertThat(tmp.resolve("2023-09-06")).exists();
    assertThat(tmp.resolve("2023-09-01")).doesNotExist();
    assertThat(tmp.resolve("2023-09-02")).doesNotExist();
  }

  @Test
  void shouldNotDeleteFiles(@TempDir Path tmp) throws IOException {
    Files.createFile(tmp.resolve("2023-09-01"));
    Files.createFile(tmp.resolve("2023-09-02"));
    Files.createFile(tmp.resolve("2023-09-03"));
    Files.createFile(tmp.resolve("2023-09-04"));
    Files.createFile(tmp.resolve("2023-09-05"));
    Files.createFile(tmp.resolve("2023-09-06"));

    cleaner.cleanup(tmp);

    assertThat(tmp.resolve("2023-09-01")).exists();
    assertThat(tmp.resolve("2023-09-02")).exists();
    assertThat(tmp.resolve("2023-09-03")).exists();
    assertThat(tmp.resolve("2023-09-04")).exists();
    assertThat(tmp.resolve("2023-09-05")).exists();
    assertThat(tmp.resolve("2023-09-06")).exists();
  }

  @Test
  void shouldNotDeleteNonMatchingFolder(@TempDir Path tmp) throws IOException {
    Files.createDirectories(tmp.resolve("A"));
    Files.createDirectories(tmp.resolve("B"));
    Files.createDirectories(tmp.resolve("C"));
    Files.createDirectories(tmp.resolve("D"));
    Files.createDirectories(tmp.resolve("E"));
    Files.createDirectories(tmp.resolve("F"));

    cleaner.cleanup(tmp);

    assertThat(tmp.resolve("A")).exists();
    assertThat(tmp.resolve("B")).exists();
    assertThat(tmp.resolve("C")).exists();
    assertThat(tmp.resolve("D")).exists();
    assertThat(tmp.resolve("E")).exists();
    assertThat(tmp.resolve("F")).exists();
  }

  @Test
  void shouldDeleteNothingWithEmptyFolder(@TempDir Path tmp) throws IOException {
    cleaner.cleanup(tmp);

    assertThat(tmp).exists();
    assertThat(tmp).isEmptyDirectory();
  }

  @Test
  void shouldNotFailWhenDirectoryDoesNotExists(@TempDir Path tmp) throws IOException {
    Path notExistingPath = tmp.resolve(".installed");
    cleaner.cleanup(notExistingPath);
  }
}

