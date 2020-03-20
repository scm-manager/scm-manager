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
    
package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.io.Resources;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.net.URL;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgWindowsPackageFixTest
{

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testHgBatModify() throws IOException
  {
    testModify(createHgBat("01"));

  }

  /**
   *  Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testHgBatModifyWithComment() throws IOException
  {
    testModify(createHgBat("02"));
  }

  /**
   *  Method description
   *
   *
   * @throws IOException
   */
  @Test
  public void testHgBatWithoutModify() throws IOException
  {
    long length = testModify(createHgBat("03")).length();

    assertEquals(0, length);
  }

  /**
   * Method description
   *
   *
   * @param number
   *
   * @return
   *
   * @throws IOException
   */
  private File createHgBat(String number) throws IOException
  {
    URL url = Resources.getResource("sonia/scm/repository/hg.bat.".concat(number));
    File file = tempFolder.newFile(number);

    try (FileOutputStream fos = new FileOutputStream(file))
    {
      Resources.copy(url, fos);
    }

    return file;
  }

  /**
   * Method description
   *
   *
   * @param file
   *
   * @return
   */
  private File testModify(File file)
  {
    HgWindowsPackageFix.fixHgPy(file);
    assertTrue(HgWindowsPackageFix.isSetBinaryAvailable(file));

    File mod = new File(file.getParentFile(), HgWindowsPackageFix.MODIFY_MARK_01);

    assertTrue(mod.exists());

    return mod;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();
}
