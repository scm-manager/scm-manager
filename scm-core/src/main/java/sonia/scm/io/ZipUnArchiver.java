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

import sonia.scm.util.IOUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class ZipUnArchiver extends AbstractUnArchiver
{

  public static final String EXTENSION = ".zip";

  private static final Logger logger =
    LoggerFactory.getLogger(ZipUnArchiver.class);


  /**
   * @since 1.21
   */
  public void extractArchive(InputStream inputStream, File outputDirectory)
    throws IOException
  {
    ZipInputStream input = new ZipInputStream(inputStream);

    ZipEntry entry = input.getNextEntry();

    while (entry != null)
    {
      extractEntry(outputDirectory, input, entry);
      entry = input.getNextEntry();
    }
  }

  @Override
  protected void extractArchive(File archive, File outputDirectory)
    throws IOException
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("extract zip \"{}\" to \"{}\"", archive.getPath(),
        outputDirectory.getAbsolutePath());
    }

    InputStream input = null;

    try
    {
      input = new FileInputStream(archive);
      extractArchive(input, outputDirectory);
    }
    finally
    {
      IOUtil.close(input);
    }
  }

  private File createFile(File outputDirectory, String name)
  {
    if (name.contains(".."))
    {
      throw new IllegalArgumentException("name is invalid");
    }

    return new File(outputDirectory, name);
  }

  private void extractEntry(File outputDirectory, ZipInputStream input,
    ZipEntry entry)
    throws IOException
  {
    try
    {
      File file = createFile(outputDirectory, entry.getName());

      if (!entry.isDirectory())
      {
        File parent = file.getParentFile();

        if (parent != null)
        {
          IOUtil.mkdirs(parent);
          extractFile(input, file);
        }
        else
        {
          logger.warn("file {} has no parent", file.getPath());
        }
      }
      else
      {
        IOUtil.mkdirs(file);
      }
    }
    finally
    {
      input.closeEntry();
    }
  }

  private void extractFile(ZipInputStream input, File outputFile)
    throws IOException
  {
    FileOutputStream output = null;

    try
    {
      output = new FileOutputStream(outputFile);
      IOUtil.copy(input, output);
    }
    finally
    {
      IOUtil.close(output);
    }
  }
}
