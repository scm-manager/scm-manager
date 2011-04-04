/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.text.ParseException;

import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 *
 * @author Sebastian Sdorra
 */
public class ScmUpgradeHandler
{

  /** the logger for ScmUpgradeHandler */
  private static final Logger logger =
    LoggerFactory.getLogger(ScmUpgradeHandler.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  public void doUpgrade()
  {
    File baseDirectory = SCMContext.getContext().getBaseDirectory();
    File configDirectory = new File(baseDirectory, "config");
    File versionFile = new File(configDirectory, "version.txt");

    // pre version 1.2
    if (!versionFile.exists())
    {
      if (logger.isInfoEnabled())
      {
        logger.info("upgrade to version {}",
                    SCMContext.getContext().getVersion());
      }

      fixDate(configDirectory);
    }

    writeVersionFile(versionFile);
  }

  /**
   * Method description
   *
   *
   * @param value
   *
   * @return
   */
  private String convertDate(String value)
  {
    if (Util.isNotEmpty(value))
    {
      try
      {
        Date date = Util.parseDate(value);

        if (date != null)
        {
          value = Long.toString(date.getTime());
        }
      }
      catch (ParseException ex)
      {
        logger.warn("could not parse date", ex);
      }
    }

    return value;
  }

  /**
   * Method description
   *
   *
   * @param configDirectory
   */
  private void fixDate(File configDirectory)
  {
    try
    {
      DocumentBuilder builder =
        DocumentBuilderFactory.newInstance().newDocumentBuilder();

      fixDate(builder, configDirectory, "users.xml");
      fixDate(builder, configDirectory, "groups.xml");
      fixDate(builder, configDirectory, "repositories.xml");
    }
    catch (Exception ex)
    {
      logger.error("could not parse document", ex);
    }
  }

  /**
   * Method description
   *
   *
   * @param builder
   * @param configDirectory
   * @param filename
   *
   * @throws IOException
   * @throws SAXException
   * @throws TransformerConfigurationException
   * @throws TransformerException
   */
  private void fixDate(DocumentBuilder builder, File configDirectory,
                       String filename)
          throws SAXException, IOException, TransformerConfigurationException,
                 TransformerException
  {
    File configFile = new File(configDirectory, filename);

    if (configFile.exists())
    {
      if (logger.isInfoEnabled())
      {
        logger.info("fix date elements of {}", configFile.getPath());
      }

      Document document = builder.parse(configFile);

      fixDate(document, "lastModified");
      fixDate(document, "creationDate");
      fixDate(document, "creationTime");
      writeDocument(document, configFile);
    }
  }

  /**
   * Method description
   *
   *
   * @param document
   * @param element
   */
  private void fixDate(Document document, String element)
  {
    NodeList nodes = document.getElementsByTagName(element);

    if (nodes != null)
    {
      for (int i = 0; i < nodes.getLength(); i++)
      {
        Node node = nodes.item(i);
        String value = node.getTextContent();

        value = convertDate(value);
        node.setTextContent(value);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param document
   * @param configFile
   *
   * @throws TransformerConfigurationException
   * @throws TransformerException
   */
  private void writeDocument(Document document, File configFile)
          throws TransformerConfigurationException, TransformerException
  {
    Transformer transformer = TransformerFactory.newInstance().newTransformer();

    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.transform(new DOMSource(document),
                          new StreamResult(configFile));
  }

  /**
   * Method description
   *
   *
   * @param versionFile
   */
  private void writeVersionFile(File versionFile)
  {
    OutputStream output = null;

    try
    {
      output = new FileOutputStream(versionFile);
      output.write(SCMContext.getContext().getVersion().getBytes());
    }
    catch (IOException ex)
    {
      logger.error("could not write version file", ex);
    }
    finally
    {
      IOUtil.close(output);
    }
  }
}
