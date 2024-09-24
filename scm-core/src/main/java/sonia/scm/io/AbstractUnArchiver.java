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

package sonia.scm.io;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.ChecksumUtil;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Properties;

public abstract class AbstractUnArchiver implements UnArchiver
{

  public static final String FILE_SOURCE_PROPERTIES = "scm-source.properties";

  public static final String PROPERTY_CHECKSUM = "scm.unarchiver.checksum";

  public static final String PROPERTY_SOURCEFILE = "scm.unarchiver.source";

  private static final Logger logger =
    LoggerFactory.getLogger(AbstractUnArchiver.class);


  protected abstract void extractArchive(File archive, File outputDirectory)
          throws IOException;

  @Override
  public void extract(File archive, File outputDirectory) throws IOException
  {
    extract(archive, outputDirectory, false);
  }

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

  private void extractAndCreateProperties(File archive, File outputDirectory)
          throws IOException
  {
    extractArchive(archive, outputDirectory);

    String checksum = ChecksumUtil.createChecksum(archive);

    writeProperties(outputDirectory, checksum);
  }

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
