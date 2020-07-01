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
 * @deprecated use {@link sonia.scm.autoconfig.AutoConfigurator}
 */
@Deprecated
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
