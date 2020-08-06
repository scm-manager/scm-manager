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

  @Test
  void shouldExtractPluginXml(@TempDir Path tempDir) throws IOException {
    Path pluginFile = createZipFile(tempDir, "META-INF/scm/plugin.xml", PLUGIN_XML);

    InstalledPluginDescriptor installedPluginDescriptor = SmpDescriptorExtractor.extractPluginDescriptor(pluginFile);

    Assertions.assertThat(installedPluginDescriptor.getInformation().getName()).isEqualTo("scm-test-plugin");
  }

  @Test
  void shouldFailWithoutPluginXml(@TempDir Path tempDir) throws IOException {
    Path pluginFile = createZipFile(tempDir, "META-INF/wrong/plugin.xml", PLUGIN_XML);

    assertThrows(IOException.class, () -> SmpDescriptorExtractor.extractPluginDescriptor(pluginFile));
  }

  @Test
  void shouldFailWithIllegalPluginXml(@TempDir Path tempDir) throws IOException {
    Path pluginFile = createZipFile(tempDir, "META-INF/scm/plugin.xml", "<not><parsable>content</parsable></not>");

    assertThrows(IOException.class, () -> SmpDescriptorExtractor.extractPluginDescriptor(pluginFile));
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
