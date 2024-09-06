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
