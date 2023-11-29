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

package sonia.scm.plugin;

import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class PluginTransformerTest {

  private static final String PLUGIN = "sonia/scm/plugin/scm-jakarta-test-plugin";

  @Test
  void shouldTransformPlugin(@TempDir Path tempDirectoryPath) throws IOException {
    Path testPluginPath = tempDirectoryPath.resolve("scm-jakarta-test-plugin");
    copyFolder(Path.of(Resources.getResource(PLUGIN).getPath()), testPluginPath);

    InstalledPlugin plugin = PluginTestHelper.createInstalled("scm-jakarta-test-plugin");
    ExplodedSmp explodedSmp = new ExplodedSmp(testPluginPath, plugin.getDescriptor());

    PluginTransformer.transform(explodedSmp.getPath());

    // Single javax import
    Path transformedGeneratorClass = tempDirectoryPath.resolve("scm-jakarta-test-plugin/classes/com/cloudogu/auditlog/AuditEntryGenerator.class");
    assertThat(transformedGeneratorClass).exists();
    try (FileInputStream fileInputStream = new FileInputStream(transformedGeneratorClass.toFile())) {
      String fileContent = new String(fileInputStream.readAllBytes());
      assertThat(fileContent).contains("jakarta");
      assertThat(fileContent).doesNotContain("javax");
    }

    // Multiple javax imports
    Path transformedDatabaseClass = tempDirectoryPath.resolve("scm-jakarta-test-plugin/classes/com/cloudogu/auditlog/AuditLogDatabase.class");
    assertThat(transformedDatabaseClass).exists();
    try (FileInputStream fileInputStream = new FileInputStream(transformedDatabaseClass.toFile())) {
      String fileContent = new String(fileInputStream.readAllBytes());
      assertThat(fileContent).contains("jakarta");
      assertThat(fileContent).doesNotContain("javax");
    }

    // No javax imports
    Path transformedDtoClass = tempDirectoryPath.resolve("scm-jakarta-test-plugin/classes/com/cloudogu/auditlog/AuditLogDto.class");
    assertThat(transformedDtoClass).exists();
    try (FileInputStream fileInputStream = new FileInputStream(transformedDtoClass.toFile())) {
      String fileContent = new String(fileInputStream.readAllBytes());
      assertThat(fileContent).doesNotContain("jakarta");
      assertThat(fileContent).doesNotContain("javax");
    }
  }

  public  void copyFolder(Path src, Path dest) throws IOException {
    try (Stream<Path> stream = Files.walk(src)) {
      stream.forEach(source -> copy(source, dest.resolve(src.relativize(source))));
    }
  }

  private void copy(Path source, Path dest) {
    try {
      Files.copy(source, dest, REPLACE_EXISTING);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
