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

package sonia.scm.util;

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static sonia.scm.util.Archives.addPathToTar;
import static sonia.scm.util.Archives.extractTar;
import static sonia.scm.util.Archives.createTarOutputStream;

class ArchivesTest {

  @Test
  void writeAndReadTar(@TempDir Path temp) throws IOException {
    Path source = temp.resolve("source");
    Files.createDirectories(source);

    Path file = source.resolve("file");
    Path fileInDirectory = source.resolve("some").resolve("very").resolve("deep").resolve("nested").resolve("directory").resolve("with").resolve("file");
    Files.createDirectories(fileInDirectory.getParent());

    Files.write(file, singleton("content"));
    Files.write(fileInDirectory, singleton("content in directory"));

    Path tarFile = temp.resolve("test.tar");
    try (OutputStream outputStream = Files.newOutputStream(tarFile)) {
      addPathToTar(source, outputStream).run();
    }

    Path target = temp.resolve("target");
    Files.createDirectories(target);
    extractTar(Files.newInputStream(tarFile), target).run();

    assertThat(target.resolve("file")).hasContent("content");
    assertThat(target.resolve("some").resolve("very").resolve("deep").resolve("nested").resolve("directory").resolve("with").resolve("file")).hasContent("content in directory");
  }
}
