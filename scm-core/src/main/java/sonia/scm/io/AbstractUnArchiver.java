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



package sonia.scm.io;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.ChecksumUtil;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Properties;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class AbstractUnArchiver implements UnArchiver
{

  /** Field description */
  public static final String FILE_SOURCE_PROPERTIES = "scm-source.properties";

  /** Field description */
  public static final String PROPERTY_CHECKSUM = "scm.unarchiver.checksum";

  /** Field description */
  public static final String PROPERTY_SOURCEFILE = "scm.unarchiver.source";

  /** the logger for AbstractUnArchiver */
  private static final Logger logger =
    LoggerFactory.getLogger(AbstractUnArchiver.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param archive
   * @param outputDirectory
   *
   * @throws IOException
   */
  protected abstract void extractArchive(File archive, File outputDirectory)
          throws IOException;

  /**
   * Method description
   *
   *
   * @param archive
   * @param outputDirectory
   *
   * @throws IOException
   */
  @Override
  public void extract(File archive, File outputDirectory) throws IOException
  {
    extract(archive, outputDirectory, false);
  }

  /**
   * Method description
   *
   *
   * @param archive
   * @param outputDirectory
   * @param force
   *
   * @throws IOException
   */
  @Override
  public void extract(File archive, File outputDirectory, boolean force)
          throws IOException
  {
    if (!outputDirectory.exists())
    {
      IOUtil.mkdirs(outputDirectory);
      extractAndCreateProperties(archive, outputDirectory);
    }
    else if (force || isModified(archive, outputDirectory))
    {
      IOUtil.delete(outputDirectory);
      IOUtil.mkdirs(outputDirectory);
      extractAndCreateProperties(archive, outputDirectory);
    }
  }

  /**
   * Method description
   *
   *
   * @param archive
   * @param outputDirectory
   *
   * @throws IOException
   */
  private void extractAndCreateProperties(File archive, File outputDirectory)
          throws IOException
  {
    extractArchive(archive, outputDirectory);

    String checksum = ChecksumUtil.createChecksum(archive);

    writeProperties(outputDirectory, checksum);
  }

  /**
   * Method description
   *
   *
   * @param outputDirectory
   * @param checksum
   *
   * @throws IOException
   */
  private void writeProperties(File outputDirectory, String checksum)
          throws IOException
  {
    Properties properties = new Properties();

    properties.setProperty(PROPERTY_CHECKSUM, checksum);
    properties.setProperty(PROPERTY_SOURCEFILE,
                           outputDirectory.getAbsolutePath());

    FileOutputStream output = null;

    try
    {
      output = new FileOutputStream(new File(outputDirectory,
              FILE_SOURCE_PROPERTIES));
      properties.store(
          output, AbstractUnArchiver.class.getName().concat(" properties"));
    }
    finally
    {
      IOUtil.close(output);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param sourcePropsFile
   *
   * @return
   */
  private String getChecksumProperty(File sourcePropsFile)
  {
    Properties properties = new Properties();
    FileInputStream input = null;

    try
    {
      input = new FileInputStream(sourcePropsFile);
      properties.load(input);
    }
    catch (IOException ex)
    {
      logger.error(ex.getMessage(), ex);
    }
    finally
    {
      IOUtil.close(input);
    }

    return properties.getProperty(PROPERTY_CHECKSUM);
  }

  /**
   * Method description
   *
   *
   * @param archive
   * @param outputDirectory
   *
   * @return
   */
  private boolean isModified(File archive, File outputDirectory)
  {
    boolean modified = true;
    File sourcePropsFile = new File(outputDirectory, FILE_SOURCE_PROPERTIES);

    if (sourcePropsFile.exists())
    {
      String checksumProperty = getChecksumProperty(sourcePropsFile);

      if (Util.isNotEmpty(checksumProperty))
      {
        try
        {
          String checksum = ChecksumUtil.createChecksum(archive);

          if (checksumProperty.equals(checksum))
          {
            if (logger.isDebugEnabled())
            {
              logger.debug("file {} is not modified", archive.getPath());
            }

            modified = false;
          }
          else if (logger.isDebugEnabled())
          {
            logger.debug("file {} is modified", archive.getPath());
          }
        }
        catch (IOException ex)
        {
          logger.error(ex.getMessage(), ex);
        }
      }
    }

    return modified;
  }
}
