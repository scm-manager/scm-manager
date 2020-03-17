/**
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
    File archive = createArchive("sonia.sample", "1.0");
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
  public void testGetPlugin() throws IOException
  {
    File archive = createArchive("sonia.sample", "1.0");
    InstalledPluginDescriptor plugin = SmpArchive.create(archive).getPlugin();

    assertNotNull(plugin);

    PluginInformation info = plugin.getInformation();

    assertNotNull(info);

    assertEquals("sonia.sample", info.getName());
    assertEquals("1.0", info.getVersion());
  }

  /**
   * Method description
   *
   * @throws IOException
   */
  @Test(expected = PluginException.class)
  public void testWithMissingName() throws IOException
  {
    File archive = createArchive( null, "1.0");

    SmpArchive.create(archive).getPlugin();
  }

  /**
   * Method description
   *
   * @throws IOException
   */
  @Test(expected = PluginException.class)
  public void testWithMissingVersion() throws IOException
  {
    File archive = createArchive("sonia.sample", null);

    SmpArchive.create(archive).getPlugin();
  }

  /**
   * Method description
   *
   *
   * @param name
   * @param version
   *
   * @return
   */
  private File createArchive(String name, String version)
  {
    File archiveFile;

    try
    {
      File descriptor = tempFolder.newFile();

      writeDescriptor(descriptor, name, version);
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
   * @param name
   * @param version
   *
   * @throws IOException
   */
  private void writeDescriptor(File descriptor, String name,
    String version)
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
        writeElement(writer, "name", name);
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
