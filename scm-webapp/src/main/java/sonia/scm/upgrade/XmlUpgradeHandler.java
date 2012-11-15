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



package sonia.scm.upgrade;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;

import sonia.scm.SCMContext;
import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;

import java.util.Date;

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
public abstract class XmlUpgradeHandler implements UpgradeHandler
{

  /**
   * the logger for XmlUpgradeHandler
   */
  private static final Logger logger =
    LoggerFactory.getLogger(XmlUpgradeHandler.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param baseDirectory
   * @param note
   *
   * @return
   */
  protected File createBackupDirectory(File baseDirectory, String note)
  {
    String date = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    File backupDirectory = new File(baseDirectory,
                             "backups".concat(File.separator).concat(date));

    IOUtil.mkdirs(backupDirectory);

    FileWriter writer = null;

    note = MessageFormat.format(note, SCMContext.getContext().getVersion());

    try
    {
      writer = new FileWriter(new File(backupDirectory, "note.txt"));
      writer.write(note);
    }
    catch (IOException ex)
    {
      logger.error("could not write note.txt for backup", ex);
    }
    finally
    {
      IOUtil.close(writer);
    }

    return backupDirectory;
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
  protected void writeDocument(Document document, File configFile)
    throws TransformerConfigurationException, TransformerException
  {
    Transformer transformer = TransformerFactory.newInstance().newTransformer();

    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.transform(new DOMSource(document),
      new StreamResult(configFile));
  }
}
