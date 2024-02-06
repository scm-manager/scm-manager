/*
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
