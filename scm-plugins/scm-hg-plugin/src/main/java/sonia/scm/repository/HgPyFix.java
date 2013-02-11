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

import com.google.common.base.Charsets;
import com.google.common.io.Closeables;
import com.google.common.io.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.SCMContextProvider;
import sonia.scm.web.HgUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Sebastian Sdorra
 */
public final class HgPyFix
{

  /** Field description */
  static final String MODIFY_MARK = ".setbinary";

  /** Field description */
  private static final String HG_BAT = "hg.bat";

  /** Field description */
  private static final String HG_PY = "hg.py";

  /**
   * the logger for HgUtil
   */
  private static final Logger logger = LoggerFactory.getLogger(HgUtil.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  private HgPyFix() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param context
   * @param config
   */
  public static void fixHgPy(SCMContextProvider context, HgConfig config)
  {
    if ((config != null) && config.isValid())
    {
      String basePath = context.getBaseDirectory().getAbsolutePath();

      String hg = config.getHgBinary();

      if (hg.startsWith(basePath) && hg.endsWith(HG_BAT))
      {
        File file = new File(hg);

        file = new File(file.getParentFile(), HG_PY);
        fixHgPy(file);
      }
    }
    else if (logger.isDebugEnabled())
    {
      logger.debug(
        "could not fix hg.py, because the configuration is not valid");
    }
  }

  /**
   * Visible for testing
   *
   *
   *
   * @param hgBat
   */
  static void fixHgPy(File hgBat)
  {

    if (hgBat.exists())
    {

      File binDirectory = hgBat.getParentFile();
      File modifyMark = new File(binDirectory, MODIFY_MARK);

      if (!modifyMark.exists())
      {
        if (logger.isDebugEnabled())
        {
          logger.debug("check hg.bat for setbinary at {}", hgBat);
        }

        if (!isSetBinaryAvailable(hgBat))
        {
          injectSetBinary(hgBat);
        }
        else
        {
          createModifyMark(modifyMark);
        }
      }
      else if (logger.isDebugEnabled())
      {
        logger.debug("hg.bat allready fixed");
      }

    }
    else if (logger.isWarnEnabled())
    {
      logger.warn("could not find hg.bat at {}", hgBat);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Visible for testing
   *
   *
   * @param hg
   *
   * @return
   */
  static boolean isSetBinaryAvailable(File hg)
  {
    boolean setBinaryAvailable = false;

    BufferedReader reader = null;

    try
    {
      reader = Files.newReader(hg, Charsets.UTF_8);

      String line = reader.readLine();

      while (line != null)
      {
        line = line.trim();

        if (line.contains("mercurial.util.setbinary") &&!line.startsWith("#"))
        {
          setBinaryAvailable = true;

          break;
        }

        line = reader.readLine();
      }
    }
    catch (IOException ex)
    {
      logger.error("could not check hg.bat", ex);
    }
    finally
    {
      Closeables.closeQuietly(reader);
    }

    return setBinaryAvailable;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param modifyMark
   */
  private static void createModifyMark(File modifyMark)
  {
    try
    {
      if (!modifyMark.createNewFile())
      {
        throw new RuntimeException("could not create modify mark");
      }
    }
    catch (IOException ex)
    {
      throw new RuntimeException("could not create modify mark", ex);
    }
  }

  /**
   * Method description
   *
   *
   * @param hg
   */
  private static void injectSetBinary(File hg)
  {
    String lineSeparator = System.getProperty("line.separator");
    File mod = new File(hg.getParentFile(), MODIFY_MARK);

    hg.renameTo(mod);

    BufferedWriter writer = null;
    BufferedReader reader = null;

    try
    {
      writer = Files.newWriter(hg, Charsets.UTF_8);
      reader = Files.newReader(mod, Charsets.UTF_8);

      String line = reader.readLine();

      while (line != null)
      {

        if (line.trim().equals("mercurial.dispatch.run()"))
        {
          writer.write("for fp in (sys.stdin, sys.stdout, sys.stderr):");
          writer.write(lineSeparator);
          writer.write("    mercurial.util.setbinary(fp)");
          writer.write(lineSeparator);
          writer.write(lineSeparator);
        }

        writer.write(line);
        writer.write(lineSeparator);
        line = reader.readLine();
      }
    }
    catch (IOException ex)
    {
      logger.error("could not check hg.bat", ex);
    }
    finally
    {
      Closeables.closeQuietly(reader);
      Closeables.closeQuietly(writer);
    }
  }
}
