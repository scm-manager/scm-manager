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

package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.io.Resources;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import sonia.scm.AbstractTestBase;
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
public abstract class ZippedRepositoryTestBase extends AbstractTestBase
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
  protected void checkDate(long date)
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
    String zippedRepositoryResource = getZippedRepositoryResource();
    extract(folder, zippedRepositoryResource);
  }

  public static void extract(File targetFolder, String zippedRepositoryResource) throws IOException {
    URL url = Resources.getResource(zippedRepositoryResource);

    try (ZipInputStream zip = new ZipInputStream(url.openStream());)
    {
      ZipEntry entry = zip.getNextEntry();

      while (entry != null)
      {
        File file = new File(targetFolder, entry.getName());
        File parent = file.getParentFile();
        if (!IOUtil.isChild(parent, file)) {
          throw new IOException("invalid zip entry name");
        }

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
          try (OutputStream output = new FileOutputStream(file))
          {
            IOUtil.copy(zip, output);
          }
        }

        zip.closeEntry();
        entry = zip.getNextEntry();
      }
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
