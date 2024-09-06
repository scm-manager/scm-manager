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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SmpDescriptorExtractorTest {

  private static final String PLUGIN_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
    "<plugin>\n" +
    "\n" +
    "  <scm-version>2</scm-version>\n" +
    "\n" +
    "  <information>\n" +
    "    <displayName>Test</displayName>\n" +
    "    <author>Cloudogu GmbH</author>\n" +
    "    <category>Testing</category>\n" +
    "  <name>scm-test-plugin</name>\n" +
    "<version>2.0.0</version>\n" +
    "<description>Collects information for support cases</description>\n" +
    "</information>\n" +
    "\n" +
    "  <conditions>\n" +
    "    <min-version>2.0.0-rc1</min-version>\n" +
    "  </conditions>\n" +
    "\n" +
    "  <resources>\n" +
    "    <script>assets/scm-support-plugin.bundle.js</script>\n" +
    "  </resources>\n" +
    "\n" +
    "</plugin>\n";

  private final SmpDescriptorExtractor extractor = new SmpDescriptorExtractor();

  @Test
  void shouldExtractPluginXml(@TempDir Path tempDir) throws IOException {
    Path pluginFile = createZipFile(tempDir, "META-INF/scm/plugin.xml", PLUGIN_XML);

    InstalledPluginDescriptor installedPluginDescriptor = extractor.extractPluginDescriptor(pluginFile);

    Assertions.assertThat(installedPluginDescriptor.getInformation().getName()).isEqualTo("scm-test-plugin");
  }

  @Test
  void shouldFailWithoutPluginXml(@TempDir Path tempDir) throws IOException {
    Path pluginFile = createZipFile(tempDir, "META-INF/wrong/plugin.xml", PLUGIN_XML);

    assertThrows(IOException.class, () -> extractor.extractPluginDescriptor(pluginFile));
  }

  @Test
  void shouldFailWithIllegalPluginXml(@TempDir Path tempDir) throws IOException {
    Path pluginFile = createZipFile(tempDir, "META-INF/scm/plugin.xml", "<not><parsable>content</parsable></not>");

    assertThrows(IOException.class, () -> extractor.extractPluginDescriptor(pluginFile));
  }

  Path createZipFile(Path tempDir, String internalFileName, String content) throws IOException {
    Path pluginFile = tempDir.resolve("scm-test-plugin.smp");
    ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(pluginFile), UTF_8);
    zipOutputStream.putNextEntry(new ZipEntry(internalFileName));
    zipOutputStream.write(content.getBytes(UTF_8));
    zipOutputStream.closeEntry();
    zipOutputStream.close();
    return pluginFile;
  }
}
