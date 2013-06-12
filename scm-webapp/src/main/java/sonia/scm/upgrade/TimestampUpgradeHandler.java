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


package sonia.scm.upgrade;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import sonia.scm.SCMContext;
import sonia.scm.plugin.PluginVersion;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.text.ParseException;

import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

/**
 *
 * @author Sebastian Sdorra
 */
public class TimestampUpgradeHandler extends XmlUpgradeHandler
{

  /**
   * the logger for TimestampUpgradeHandler
   */
  private static final Logger logger =
    LoggerFactory.getLogger(TimestampUpgradeHandler.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param homeDirectory
   * @param configDirectory
   * @param oldVersion
   * @param newVersion
   */
  @Override
  public void doUpgrade(File homeDirectory, File configDirectory,
    PluginVersion oldVersion, PluginVersion newVersion)
  {
    if (oldVersion.isOlder("1.2"))
    {
      if (logger.isInfoEnabled())
      {
        logger.info("data format is older than 1.2, upgrade to version {}",
          SCMContext.getContext().getVersion());
      }

      fixDate(homeDirectory, configDirectory);
    }
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
    if (!Strings.isNullOrEmpty(value))
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
   *
   * @param baseDirectory
   * @param configDirectory
   */
  private void fixDate(File baseDirectory, File configDirectory)
  {
    try
    {
      DocumentBuilder builder =
        DocumentBuilderFactory.newInstance().newDocumentBuilder();
      File backupDirectory = createBackupDirectory(baseDirectory,
                               "upgrade to version {0}");

      fixDate(builder, configDirectory, backupDirectory, "users.xml");
      fixDate(builder, configDirectory, backupDirectory, "groups.xml");
      fixDate(builder, configDirectory, backupDirectory, "repositories.xml");
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
   * @param backupDirectory
   * @param filename
   *
   * @throws IOException
   * @throws SAXException
   * @throws TransformerConfigurationException
   * @throws TransformerException
   */
  private void fixDate(DocumentBuilder builder, File configDirectory,
    File backupDirectory, String filename)
    throws SAXException, IOException, TransformerConfigurationException,
    TransformerException
  {
    File configFile = new File(configDirectory, filename);
    File backupFile = new File(backupDirectory, filename);

    IOUtil.copy(configFile, backupFile);

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
}
