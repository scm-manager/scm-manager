package sonia.scm.plugin;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(TempDirectory.class)
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
  void shouldExtractPluginXml(@TempDirectory.TempDir Path tempDir) throws IOException {
    Path pluginFile = createZipFile(tempDir, "META-INF/scm/plugin.xml", PLUGIN_XML);

    InstalledPluginDescriptor installedPluginDescriptor = new SmpDescriptorExtractor().extractPluginDescriptor(pluginFile);

    Assertions.assertThat(installedPluginDescriptor.getInformation().getName()).isEqualTo("scm-test-plugin");
  }

  @Test
  void shouldFailWithoutPluginXml(@TempDirectory.TempDir Path tempDir) throws IOException {
    Path pluginFile = createZipFile(tempDir, "META-INF/wrong/plugin.xml", PLUGIN_XML);

    assertThrows(IOException.class, () -> new SmpDescriptorExtractor().extractPluginDescriptor(pluginFile));
  }

  @Test
  void shouldFailWithIllegalPluginXml(@TempDirectory.TempDir Path tempDir) throws IOException {
    Path pluginFile = createZipFile(tempDir, "META-INF/scm/plugin.xml", "<not><parsable>content</parsable></not>");

    assertThrows(IOException.class, () -> new SmpDescriptorExtractor().extractPluginDescriptor(pluginFile));
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
