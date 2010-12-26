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

import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author Sebastian Sdorra
 */
public class ZipUnArchiver extends AbstractUnArchiver
{

  /** Field description */
  public static final String EXTENSION = ".zip";

  /** the logger for ZipUnArchiver */
  private static final Logger logger =
    LoggerFactory.getLogger(ZipUnArchiver.class);

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
  @Override
  protected void extractArchive(File archive, File outputDirectory)
          throws IOException
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("extract zip \"{}\" to \"{}\"", archive.getPath(),
                   outputDirectory.getAbsolutePath());
    }

    ZipInputStream input = null;

    try
    {
      input = new ZipInputStream(new FileInputStream(archive));

      ZipEntry entry = input.getNextEntry();

      while (entry != null)
      {
        extractEntry(outputDirectory, input, entry);
        entry = input.getNextEntry();
      }
    }
    finally
    {
      IOUtil.close(input);
    }
  }

  /**
   * Method description
   *
   *
   * @param outputDirectory
   * @param name
   *
   * @return
   */
  private File createFile(File outputDirectory, String name)
  {
    if (name.contains(".."))
    {
      throw new IllegalArgumentException("name is invalid");
    }

    return new File(outputDirectory, name);
  }

  /**
   * Method description
   *
   *
   * @param outputDirectory
   * @param input
   * @param entry
   *
   * @throws IOException
   */
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

  /**
   * Method description
   *
   *
   * @param input
   * @param outputFile
   *
   * @throws IOException
   */
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
