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
