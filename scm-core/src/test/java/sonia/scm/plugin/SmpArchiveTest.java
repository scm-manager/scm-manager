/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.io.Files;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.w3c.dom.Document;

import org.xml.sax.SAXException;

import sonia.scm.util.IOUtil;
import sonia.scm.util.XmlUtil;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author Sebastian Sdorra
 */
public class SmpArchiveTest
{

  /**
   * Method description
   *
   *
   * @throws IOException
   * @throws ParserConfigurationException
   * @throws SAXException
   */
  @Test
  public void testExtract()
    throws IOException, ParserConfigurationException, SAXException
  {
    File archive = createArchive("sonia.sample", "sample", "1.0");
    File target = tempFolder.newFolder();

    IOUtil.mkdirs(target);
    SmpArchive.create(archive).extract(target);

    File descriptor = new File(target, PluginConstants.FILE_DESCRIPTOR);

    assertTrue(descriptor.exists());

    try (FileInputStream fis = new FileInputStream(descriptor))
    {
      Document doc = XmlUtil.createDocument(fis);

      assertEquals("plugin", doc.getDocumentElement().getNodeName());
    }
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testGetDescriptorDocument() throws IOException
  {
    File archive = createArchive("sonia.sample", "sample", "1.0");
    Document doc = SmpArchive.create(archive).getDescriptorDocument();

    assertNotNull(doc);
    assertEquals("plugin", doc.getDocumentElement().getNodeName());
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testGetPluginId() throws IOException
  {
    File archive = createArchive("sonia.sample", "sample", "1.0");
    PluginId pluginId = SmpArchive.create(archive).getPluginId();

    assertNotNull(pluginId);
    assertEquals("sonia.sample", pluginId.getGroupId());
    assertEquals("sample", pluginId.getArtifactId());
    assertEquals("1.0", pluginId.getVersion());
  }

  /**
   * Method description
   *
   * @throws IOException
   */
  @Test(expected = PluginException.class)
  public void testWithMissingArtifactId() throws IOException
  {
    File archive = createArchive("sonia.sample", null, "1.0");

    SmpArchive.create(archive).getPluginId();
  }

  /**
   * Method description
   *
   * @throws IOException
   */
  @Test(expected = PluginException.class)
  public void testWithMissingGroupId() throws IOException
  {
    File archive = createArchive(null, "sample", "1.0");

    SmpArchive.create(archive).getPluginId();
  }

  /**
   * Method description
   *
   * @throws IOException
   */
  @Test(expected = PluginException.class)
  public void testWithMissingVersion() throws IOException
  {
    File archive = createArchive("sonia.sample", "sample", null);

    SmpArchive.create(archive).getPluginId();
  }

  /**
   * Method description
   *
   *
   * @param groupId
   * @param artifactId
   * @param version
   *
   * @return
   */
  private File createArchive(String groupId, String artifactId, String version)
  {
    File archiveFile;

    try
    {
      File descriptor = tempFolder.newFile();

      writeDescriptor(descriptor, groupId, artifactId, version);
      archiveFile = tempFolder.newFile();

      try (ZipOutputStream zos =
        new ZipOutputStream(new FileOutputStream(archiveFile), Charsets.UTF_8))
      {
        zos.putNextEntry(new ZipEntry(PluginConstants.PATH_DESCRIPTOR));
        Files.copy(descriptor, zos);
        zos.closeEntry();
        zos.putNextEntry(new ZipEntry("/META-INF/"));
        zos.putNextEntry(new ZipEntry("/META-INF/somefile.txt"));
        zos.write("some text".getBytes(Charsets.UTF_8));
        zos.closeEntry();
      }
    }
    catch (IOException ex)
    {
      throw Throwables.propagate(ex);
    }

    return archiveFile;
  }

  /**
   * Method description
   *
   *
   * @param file
   *
   * @return
   *
   * @throws IOException
   * @throws XMLStreamException
   */
  private XMLStreamWriter createStreamWriter(File file)
    throws IOException, XMLStreamException
  {
    return XMLOutputFactory.newFactory().createXMLStreamWriter(
      new FileOutputStream(file));
  }

  /**
   * Method description
   *
   *
   * @param descriptor
   * @param groupId
   * @param artifactId
   * @param version
   *
   * @throws IOException
   */
  private void writeDescriptor(File descriptor, String groupId,
    String artifactId, String version)
    throws IOException
  {
    try
    {

      IOUtil.mkdirs(descriptor.getParentFile());

      XMLStreamWriter writer = null;

      try
      {
        writer = createStreamWriter(descriptor);
        writer.writeStartDocument();
        writer.writeStartElement("plugin");
        writer.writeStartElement("information");
        writeElement(writer, "groupId", groupId);
        writeElement(writer, "artifactId", artifactId);
        writeElement(writer, "version", version);

        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndDocument();
      }
      finally
      {
        if (writer != null)
        {
          writer.close();
        }
      }
    }
    catch (XMLStreamException ex)
    {
      throw Throwables.propagate(ex);
    }
  }

  /**
   * Method description
   *
   *
   * @param writer
   * @param name
   * @param value
   *
   * @throws XMLStreamException
   */
  private void writeElement(XMLStreamWriter writer, String name, String value)
    throws XMLStreamException
  {
    if (!Strings.isNullOrEmpty(value))
    {
      writer.writeStartElement(name);
      writer.writeCharacters(value);
      writer.writeEndElement();
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();
}
