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



package sonia.scm.installer;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.io.SimpleCommand;
import sonia.scm.io.SimpleCommandResult;
import sonia.scm.repository.HgConfig;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import java.util.Scanner;

/**
 *
 * @author Sebastian Sdorra
 */
public class WindowsHgInstaller extends AbstractHgInstaller
{

  /** Field description */
  public static String[] PATH_HG = new String[]
  {

    // TortoiseHg
    "TortoiseHg\\hg.exe"
  };

  /** Field description */
  public static String[] PATH_LIBRARY_ZIP = new String[]
  {

    // TortoiseHg
    "TortoiseHg\\library.zip"
  };

  /** Field description */
  public static String[] PATH_TEMPLATE = new String[]
  {

    // TortoiseHg
    "TortoiseHg\\template"
  };

  /** Field description */
  private static final String DEFAULT_PROGRAMMDIRECTORY =
    "C:\\Programm Files\\";

  /** the logger for WindowsHgInstaller */
  private static final Logger logger =
    LoggerFactory.getLogger(WindowsHgInstaller.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param baseDirectory
   */
  public WindowsHgInstaller(File baseDirectory)
  {
    super(baseDirectory);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param config
   *
   * @throws IOException
   */
  @Override
  public void install(HgConfig config) throws IOException
  {
    super.install(config);

    String progDir = getProgrammDirectory();
    String path = null;
    File libraryZip = find(progDir, PATH_LIBRARY_ZIP);

    if (libraryZip != null)
    {
      File libDir = new File(baseDirectory, "lib\\hg");

      IOUtil.extract(libraryZip, libDir);
      path = libDir.getAbsolutePath();
    }

    File templateDir = find(progDir, PATH_TEMPLATE);

    if (templateDir != null)
    {
      if (path != null)
      {
        path = path.concat(";").concat(templateDir.getAbsolutePath());
      }
      else
      {
        path = templateDir.getAbsolutePath();
      }
    }

    if (path != null)
    {
      config.setPythonPath(path);
    }

    checkForOptimizedByteCode(config);
    config.setPythonBinary(getPythonBinary());
  }

  /**
   * Method description
   *
   *
   * @param config
   */
  @Override
  public void update(HgConfig config) {}

  /**
   * Method description
   *
   *
   * @param config
   */
  private void checkForOptimizedByteCode(HgConfig config)
  {
    boolean optimized = false;
    String path = config.getPythonPath();

    if (Util.isNotEmpty(path))
    {
      for (String part : path.split(";"))
      {
        if (checkForOptimizedByteCode(part))
        {
          optimized = true;

          break;
        }
      }
    }

    config.setUseOptimizedBytecode(optimized);
  }

  /**
   * Method description
   *
   *
   * @param part
   *
   * @return
   */
  private boolean checkForOptimizedByteCode(String part)
  {
    File libDir = new File(part);
    String[] pyoFiles = libDir.list(new FilenameFilter()
    {
      @Override
      public boolean accept(File file, String name)
      {
        return name.toLowerCase().endsWith(".pyo");
      }
    });

    return Util.isNotEmpty(pyoFiles);
  }

  /**
   * Method description
   *
   *
   * @param prefix
   * @param path
   *
   * @return
   */
  private File find(String prefix, String[] path)
  {
    File result = null;

    for (String pathPart : path)
    {
      File file = new File(prefix, pathPart);

      if (file.exists())
      {
        result = file;

        break;
      }
    }

    return result;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  private String getProgrammDirectory()
  {
    return getRegistryValue(
        "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion",
        "ProgramFilesDir", DEFAULT_PROGRAMMDIRECTORY);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private String getPythonBinary()
  {
    String python =
      getRegistryValue(
          "﻿HKEY_CLAS﻿SES_ROOT\\Python.File\\shell\\open\\command", null, null);

    if (python == null)
    {
      python = search(new String[0], "python");
    }

    return python;
  }

  /**
   * Method description
   *
   *
   * @param key
   * @param subKey
   * @param defaultValue
   *
   * @return
   */
  private String getRegistryValue(String key, String subKey,
                                  String defaultValue)
  {
    String programmDirectory = defaultValue;
    SimpleCommand command = null;

    if (subKey != null)
    {
      command = new SimpleCommand("reg", "query", key, "/v", subKey);
    }
    else
    {
      command = new SimpleCommand("reg", "query", key);
    }

    try
    {
      SimpleCommandResult result = command.execute();

      if (result.isSuccessfull())
      {
        String output = result.getOutput();
        Scanner scanner = new Scanner(output);

        while (scanner.hasNextLine())
        {
          String line = scanner.nextLine();
          int index = line.indexOf("﻿REG_SZ");

          if (index > 0)
          {
            programmDirectory = line.substring(index
                                               + "﻿REG_SZ".length()).trim();

            if (logger.isDebugEnabled())
            {
              logger.debug("use programm directory {}", programmDirectory);
            }
          }
        }
      }
    }
    catch (IOException ex)
    {
      logger.error(ex.getMessage(), ex);
    }

    return programmDirectory;
  }
}
