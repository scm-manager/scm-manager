package sonia.scm.plugin;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class SmpDescriptorExtractor {

  InstalledPluginDescriptor extractPluginDescriptor(Path file) throws IOException {
    try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(file), StandardCharsets.UTF_8))    {
      ZipEntry nextEntry;
      while ((nextEntry = zipInputStream.getNextEntry()) != null) {
        if ("META-INF/scm/plugin.xml".equals(nextEntry.getName())) {
          JAXBContext context = JAXBContext.newInstance(ScmModule.class, InstalledPluginDescriptor.class);
          return (InstalledPluginDescriptor) context.createUnmarshaller().unmarshal(zipInputStream);
        }
      }
    } catch (JAXBException e) {
      throw new IOException("failed to read descriptor file META-INF/scm/plugin.xml from plugin", e);
    }
    throw new IOException("Missing plugin descriptor META-INF/scm/plugin.xml in download package");
  }
}
