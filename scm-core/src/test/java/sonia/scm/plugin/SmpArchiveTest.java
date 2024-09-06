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

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import sonia.scm.util.IOUtil;
import sonia.scm.util.XmlUtil;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class SmpArchiveTest {

  @Test
  void shouldExtractArchive(@TempDir Path tempDir) throws IOException, ParserConfigurationException, SAXException, XMLStreamException {
    File archive = createArchive(tempDir, "sonia.sample", "1.0");
    File target = tempDir.toFile();

    IOUtil.mkdirs(target);
    SmpArchive.create(archive).extract(target);

    File descriptor = new File(target, PluginConstants.FILE_DESCRIPTOR);

    assertThat(descriptor).exists();

    try (FileInputStream fis = new FileInputStream(descriptor)) {
      Document doc = XmlUtil.createDocument(fis);
      assertThat(doc.getDocumentElement().getNodeName()).isEqualTo("plugin");
    }
  }

  @Test
  void shouldReturnPluginDescriptor(@TempDir Path tempDir) throws IOException, XMLStreamException {
    File archive = createArchive(tempDir, "sonia.sample", "1.0");
    InstalledPluginDescriptor plugin = SmpArchive.create(archive).getPlugin();

    assertThat(plugin).isNotNull();

    PluginInformation info = plugin.getInformation();

    assertThat(info).isNotNull();

    assertThat(info.getName()).isEqualTo("sonia.sample");
    assertThat(info.getVersion()).isEqualTo("1.0");
  }

  @Test
  void shouldFailOnMissingName(@TempDir Path tempDir) throws IOException, XMLStreamException {
    File archive = createArchive(tempDir, null, "1.0");

    SmpArchive smp = SmpArchive.create(archive);
    assertThrows(PluginException.class, smp::getPlugin);
  }

  @Test
  void shouldFailOnMissingVersion(@TempDir Path tempDir) throws IOException, XMLStreamException {
    File archive = createArchive(tempDir, "sonia.sample", null);
    SmpArchive smp = SmpArchive.create(archive);
    assertThrows(PluginException.class, smp::getPlugin);
  }

  @Test
  void shouldFailOnZipEntriesWhichCreateFilesOutsideOfThePluginFolder(@TempDir Path tempDir) throws IOException {
    ZipInputStream zis = mock(ZipInputStream.class);
    ZipEntry entry = mock(ZipEntry.class);
    when(zis.getNextEntry()).thenReturn(entry);
    when(entry.getName()).thenReturn("../../plugin.xml");
    SmpArchive smp = new SmpArchive(ByteSource.empty(), source -> zis);
    File directory = tempDir.resolve("one").resolve("two").resolve("three").toFile();
    assertThat(directory.mkdirs()).isTrue();
    assertThrows(PluginException.class, () -> smp.extract(directory));
  }

  private File createArchive(Path tempDir, String name, String version) throws IOException, XMLStreamException {
    File descriptor = tempDir.resolve("descriptor.xml").toFile();

    writeDescriptor(descriptor, name, version);
    File archiveFile = tempDir.resolve("archive.smp").toFile();

    try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(archiveFile), Charsets.UTF_8)) {
      zos.putNextEntry(new ZipEntry(PluginConstants.PATH_DESCRIPTOR));
      Files.copy(descriptor, zos);
      zos.closeEntry();
      zos.putNextEntry(new ZipEntry("/META-INF/"));
      zos.putNextEntry(new ZipEntry("/META-INF/somefile.txt"));
      zos.write("some text".getBytes(Charsets.UTF_8));
      zos.closeEntry();
    }

    return archiveFile;
  }

  private void writeDescriptor(File descriptor, String name, String version) throws IOException, XMLStreamException {
    IOUtil.mkdirs(descriptor.getParentFile());

    XMLStreamWriter writer = null;

    try (OutputStream os = new FileOutputStream(descriptor)) {
      writer = XMLOutputFactory.newFactory().createXMLStreamWriter(os);

      writer.writeStartDocument();
      writer.writeStartElement("plugin");
      writer.writeStartElement("information");
      writeElement(writer, "name", name);
      writeElement(writer, "version", version);

      writer.writeEndElement();
      writer.writeEndElement();
      writer.writeEndDocument();
      writer.close();
    }
  }

  private void writeElement(XMLStreamWriter writer, String name, String value) throws XMLStreamException {
    if (!Strings.isNullOrEmpty(value)) {
      writer.writeStartElement(name);
      writer.writeCharacters(value);
      writer.writeEndElement();
    }
  }
}
