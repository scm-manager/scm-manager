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

package sonia.scm.repository.spi;


import com.google.common.io.Resources;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import sonia.scm.AbstractTestBase;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.util.IOUtil;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.net.URL;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public abstract class ZippedRepositoryTestBase extends AbstractTestBase
{
  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  protected Repository repository = createRepository();

  protected File repositoryDirectory;
  
  protected abstract String getType();

  
  protected abstract String getZippedRepositoryResource();


   @Before
  public void before()
  {
    repositoryDirectory = createRepositoryDirectory();
  }


  protected void checkDate(long date)
  {
    assertNotNull(date);
    assertTrue("Date should not be older than current date",
      date < System.currentTimeMillis());
  }

  
  protected Repository createRepository()
  {
    return RepositoryTestData.createHeartOfGold(getType());
  }

  
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

}
