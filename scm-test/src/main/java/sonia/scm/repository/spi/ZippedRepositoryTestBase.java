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



package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.io.Closeables;
import com.google.common.io.Resources;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.util.IOUtil;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.net.URL;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class ZippedRepositoryTestBase
{

  /**
   * Method description
   *
   *
   * @return
   */
  protected abstract String getType();

  /**
   * Method description
   *
   *
   * @return
   */
  protected abstract String getZippedRepositoryResource();

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Before
  public void before()
  {
    repositoryDirectory = createRepositoryDirectory();
  }

  /**
   * Method description
   *
   *
   * @param date
   */
  protected void checkDate(Long date)
  {
    assertNotNull(date);
    assertTrue("Date should not be older than current date",
               date < System.currentTimeMillis());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  protected Repository createRepository()
  {
    return RepositoryTestData.createHeartOfGold(getType());
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  protected File createRepositoryDirectory()
  {
    File folder = null;

    try
    {
      folder = tempFolder.newFolder();
      folder.mkdirs();
      extract(folder);
    }
    catch (IOException ex)
    {
      fail(ex.getMessage());
    }

    return folder;
  }

  /**
   * Method description
   *
   *
   * @param folder
   *
   * @throws IOException
   */
  private void extract(File folder) throws IOException
  {
    URL url = Resources.getResource(getZippedRepositoryResource());
    ZipInputStream zip = null;

    try
    {
      zip = new ZipInputStream(url.openStream());

      ZipEntry entry = zip.getNextEntry();

      while (entry != null)
      {
        File file = new File(folder, entry.getName());
        File parent = file.getParentFile();

        if (!parent.exists())
        {
          parent.mkdirs();
        }

        if (entry.isDirectory())
        {
          file.mkdirs();
        }
        else
        {
          OutputStream output = null;

          try
          {
            output = new FileOutputStream(file);
            IOUtil.copy(zip, output);
          }
          finally
          {
            Closeables.closeQuietly(output);
          }
        }

        zip.closeEntry();
        entry = zip.getNextEntry();
      }
    }
    finally
    {
      Closeables.closeQuietly(zip);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  /** Field description */
  protected Repository repository = createRepository();

  /** Field description */
  protected File repositoryDirectory;
}
