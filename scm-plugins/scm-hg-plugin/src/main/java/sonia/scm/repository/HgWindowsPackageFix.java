/**
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

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.SCMContextProvider;
import sonia.scm.util.IOUtil;
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
public final class HgWindowsPackageFix
{

  /** Field description */
  static final String MODIFY_MARK_01 = ".setbinary";

  /** Field description */
  static final String MODIFY_MARK_02 = ".setpythonpath";

  /** Field description */
  private static final String HG_BAT = "hg.bat";

  /** Field description */
  private static final String HG_PY = "hg.py";

  /** Field description */
  private static final String PYTHONPATH_FIXED =
    "set PYTHONPATH=%~dp0..\\lib;%PYTHONHOME%\\Lib;%PYTHONPATH%";

  /** Field description */
  private static final String PYTHONPATH_WRONG =
    "set PYTHONPATH=%~dp0..\\lib;%PYTHONHOME%\\Lib";

  /**
   * the logger for HgUtil
   */
  private static final Logger logger = LoggerFactory.getLogger(HgUtil.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  private HgWindowsPackageFix() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param context
   * @param config
   */
  public static void fixHgPackage(SCMContextProvider context, HgConfig config)
  {
    if ((config != null) && config.isValid())
    {
      String basePath = context.getBaseDirectory().getAbsolutePath();

      String hg = config.getHgBinary();

      if (hg.startsWith(basePath) && hg.endsWith(HG_BAT))
      {
        File file = new File(hg);

        fixHgBat(file);

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
   * @param hgBat
   */
  static void fixHgBat(File hgBat)
  {
    if (hgBat.exists())
    {
      File binDirectory = hgBat.getParentFile();
      File modifyMark = new File(binDirectory, MODIFY_MARK_02);

      if (!modifyMark.exists())
      {
        try
        {
          String content = Files.toString(hgBat, Charsets.UTF_8);

          if (!content.contains(PYTHONPATH_FIXED))
          {
            content = content.replace(PYTHONPATH_WRONG, PYTHONPATH_FIXED);
            Files.write(content, hgBat, Charsets.UTF_8);
          }

          createModifyMark(modifyMark);
        }
        catch (IOException ex)
        {
          logger.error("could not read content of {}", hgBat);

          throw Throwables.propagate(ex);
        }
      }
      else
      {
        logger.debug("hg.bat allready fixed");
      }
    }
    else
    {
      logger.warn("could not find hg.bat at {}", hgBat);
    }
  }

  /**
   * Visible for testing
   *
   *
   *
   * @param hgPy
   */
  static void fixHgPy(File hgPy)
  {

    if (hgPy.exists())
    {

      File binDirectory = hgPy.getParentFile();
      File modifyMark = new File(binDirectory, MODIFY_MARK_01);

      if (!modifyMark.exists())
      {
        if (logger.isDebugEnabled())
        {
          logger.debug("check hg.py for setbinary at {}", hgPy);
        }

        if (!isSetBinaryAvailable(hgPy))
        {
          injectSetBinary(hgPy);
        }
        else
        {
          createModifyMark(modifyMark);
        }
      }
      else if (logger.isDebugEnabled())
      {
        logger.debug("hg.py allready fixed");
      }

    }
    else if (logger.isWarnEnabled())
    {
      logger.warn("could not find hg.py at {}", hgPy);
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
      IOUtil.close(reader);
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
    File mod = new File(hg.getParentFile(), MODIFY_MARK_01);

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
      IOUtil.close(reader);
      IOUtil.close(writer);
    }
  }
}
