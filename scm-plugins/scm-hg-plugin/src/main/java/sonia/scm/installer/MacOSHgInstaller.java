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

import sonia.scm.io.DirectoryFileFilter;
import sonia.scm.repository.HgConfig;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Sebastian Sdorra
 */
public class MacOSHgInstaller extends UnixHgInstaller
{

  /** Field description */
  public static final String ENV_PATH = "PATH";

  /** Field description */
  public static final String PATH_HG = "hg";

  /** Field description */
  public static final String PATH_HG_BREW = "/usr/local/bin/hg";

  /** Field description */
  public static final String PATH_HG_BREW_INSTALLATION =
    "/usr/local/Cellar/mercurial";

  /** Field description */
  public static final String PATH_PYTHON = "python";

  /** the logger for MacOSHgInstaller */
  private static final Logger logger =
    LoggerFactory.getLogger(MacOSHgInstaller.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param baseDirectory
   * @param config
   *
   * @throws IOException
   */
  @Override
  public void install(File baseDirectory, HgConfig config) throws IOException
  {
    super.install(baseDirectory, config);

    String hg = config.getHgBinary();

    if (PATH_HG.equals(hg))
    {
      hg = resolvePath(hg);
    }

    if (PATH_HG_BREW.equals(hg))
    {
      File file = new File(PATH_HG_BREW);

      file = file.getCanonicalFile();

      if (file.getAbsolutePath().startsWith(PATH_HG_BREW_INSTALLATION))
      {
        useHomebrewInstallation(config, file);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param parent
   *
   * @return
   */
  private File findPythonDirectory(File parent)
  {
    File pythonDirectory = null;

    for (File d : parent.listFiles(DirectoryFileFilter.instance))
    {
      if (d.getName().startsWith("python"))
      {
        pythonDirectory = d;

        break;
      }
    }

    return pythonDirectory;
  }

  /**
   * Method description
   *
   *
   *
   * @param binaryName
   * @return
   */
  private String resolvePath(String binaryName)
  {
    String binary = binaryName;

    try
    {
      String path = System.getenv(ENV_PATH);

      for (String p : path.split(":"))
      {
        File file = new File(p, binaryName);

        if (file.exists())
        {
          binary = file.getAbsolutePath();

          if (logger.isDebugEnabled())
          {
            logger.debug("resolve {} path to {}", binaryName, binary);
          }

          break;
        }
      }
    }
    catch (Exception ex)
    {
      logger.error("could not resolve binary path", ex);
    }

    return binary;
  }

  /**
   * Method description
   *
   *
   * @param config
   * @param file
   */
  private void useHomebrewInstallation(HgConfig config, File file)
  {
    File parent = file.getParentFile().getParentFile();
    File libDirectory = new File(parent, "lib");

    if (!libDirectory.exists())
    {
      libDirectory = new File(parent, "libexec");
    }

    if (libDirectory.exists())
    {
      File pythonDirectory = findPythonDirectory(libDirectory);

      if (pythonDirectory != null)
      {
        File sitePackageDirectory = new File(pythonDirectory, "site-packages");

        if (sitePackageDirectory.exists())
        {
          libDirectory = sitePackageDirectory;
        }

        String pythonPath = libDirectory.getPath();

        if (logger.isInfoEnabled())
        {
          logger.info("found mercurial brew install set python path to {}",
            pythonPath);
        }

        config.setPythonPath(pythonPath);
      }
    }

    String python = config.getPythonBinary();

    if (PATH_PYTHON.equals(python))
    {
      config.setPythonBinary(resolvePath(python));
    }
  }
}
