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


package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.io.Closeables;
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
public class HgPyFixTest
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
    URL url =
      Resources.getResource("sonia/scm/repository/hg.bat.".concat(number));
    File file = tempFolder.newFile(number);
    FileOutputStream fos = null;

    try
    {
      fos = new FileOutputStream(file);
      Resources.copy(url, fos);
    }
    finally
    {
      Closeables.closeQuietly(fos);
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
    HgPyFix.fixHgPy(file);
    assertTrue(HgPyFix.isSetBinaryAvailable(file));

    File mod = new File(file.getParentFile(), HgPyFix.MODIFY_MARK);

    assertTrue(mod.exists());

    return mod;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();
}
