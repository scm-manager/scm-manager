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


package sonia.scm.installer;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.repository.HgConfig;
import sonia.scm.util.IOUtil;
import sonia.scm.util.RegistryUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class WindowsHgInstaller extends AbstractHgInstaller
{

  /** Field description */
  private static final String FILE_LIBRARY_ZIP = "library.zip";

  /** Field description */
  private static final String FILE_LIB_MERCURIAL =
    "Lib\\site-packages\\mercurial";

  /** Field description */
  private static final String FILE_MERCURIAL_EXE = "hg.exe";

  /** Field description */
  private static final String FILE_MERCURIAL_SCRIPT = "hg.bat";

  /** Field description */
  private static final String FILE_SCRIPTS = "Scripts";

  /** Field description */
  private static final String FILE_TEMPLATES = "templates";

  /** Field description */
  private static final String[] REGISTRY_HG = new String[]
  {

    // TortoiseHg
    "HKEY_CURRENT_USER\\Software\\TortoiseHg",

    // Mercurial
    "HKEY_CURRENT_USER\\Software\\Mercurial\\InstallDir"
  };

  /** Field description */
  private static final String[] REGISTRY_PYTHON = new String[]
  {

    // .py files
    "HKEY_CLASSES_ROOT\\Python.File\\shell\\open\\command"
  };

  /** the logger for WindowsHgInstaller */
  private static final Logger logger =
    LoggerFactory.getLogger(WindowsHgInstaller.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
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
    if (Util.isEmpty(config.getPythonBinary()))
    {
      String pythonBinary = getPythonBinary();

      config.setPythonBinary(pythonBinary);
    }

    if (Util.isEmpty(config.getHgBinary()))
    {
      File hgScript = getMercurialScript(config.getPythonBinary());

      if (hgScript != null)
      {
        config.setHgBinary(hgScript.getAbsolutePath());
      }
    }

    File hgDirectory = getMercurialDirectory(config.getHgBinary());

    if (hgDirectory != null)
    {
      installHg(baseDirectory, config, hgDirectory);
    }

    checkForOptimizedByteCode(config);
  }

  /**
   * Method description
   *
   *
   *
   * @param baseDirectory
   * @param config
   */
  @Override
  public void update(File baseDirectory, HgConfig config) {}

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public List<String> getHgInstallations()
  {
    return getInstallations(REGISTRY_HG);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public List<String> getPythonInstallations()
  {
    return getInstallations(REGISTRY_PYTHON);
  }

  //~--- methods --------------------------------------------------------------

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
   *
   * @param baseDirectory
   * @param config
   * @param hgDirectory
   *
   * @throws IOException
   */
  private void installHg(File baseDirectory, HgConfig config, File hgDirectory)
          throws IOException
  {
    if (logger.isInfoEnabled())
    {
      logger.info("installing mercurial {}", hgDirectory.getAbsolutePath());
    }

    File libDir = new File(baseDirectory, "lib\\hg");

    IOUtil.mkdirs(libDir);

    File libraryZip = new File(hgDirectory, FILE_LIBRARY_ZIP);

    if (libraryZip.exists())
    {
      IOUtil.extract(libraryZip, libDir);
      config.setPythonPath(libDir.getAbsolutePath());
    }

    File templateDirectory = new File(hgDirectory, FILE_TEMPLATES);

    if (templateDirectory.exists())
    {
      IOUtil.copy(templateDirectory, new File(libDir, FILE_TEMPLATES));
    }

    File hg = new File(hgDirectory, FILE_MERCURIAL_EXE);

    if (hg.exists())
    {
      config.setHgBinary(hg.getAbsolutePath());
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param registryKeys
   *
   * @return
   */
  private List<String> getInstallations(String[] registryKeys)
  {
    List<String> installations = new ArrayList<String>();

    for (String registryKey : registryKeys)
    {
      String path = RegistryUtil.getRegistryValue(registryKey);

      if (path != null)
      {
        File file = new File(path, FILE_MERCURIAL_EXE);

        if (file.exists())
        {
          installations.add(file.getAbsolutePath());
        }
      }
    }

    return installations;
  }

  /**
   * Method description
   *
   *
   *
   * @param hgBinary
   * @return
   */
  private File getMercurialDirectory(String hgBinary)
  {
    File directory = null;

    if (Util.isNotEmpty(hgBinary))
    {
      File hg = new File(hgBinary);

      if (hg.exists() && hg.isFile())
      {
        directory = hg.getParentFile();
      }
    }

    if (directory == null)
    {
      directory = getMercurialDirectoryFromRegistry();
    }

    return directory;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private File getMercurialDirectoryFromRegistry()
  {
    File directory = null;

    for (String registryKey : REGISTRY_HG)
    {
      String path = RegistryUtil.getRegistryValue(registryKey);

      if (path != null)
      {
        directory = new File(path);

        if (!directory.exists())
        {
          directory = null;
        }
        else
        {
          break;
        }
      }
    }

    return directory;
  }

  /**
   * Returns the location of the script to run Mercurial, if Mercurial is
   * installed as a Python package from source.  Only packages that include a
   * templates directory will be recognized.
   *
   * @param pythonBinary
   *
   * @return
   */
  private File getMercurialScript(String pythonBinary)
  {
    File hgScript = null;

    if (pythonBinary != null)
    {
      File pythonBinaryFile = new File(pythonBinary);

      if (pythonBinaryFile.exists())
      {
        File pythonDir = pythonBinaryFile.getParentFile();
        File scriptsDir = new File(pythonDir, FILE_SCRIPTS);
        File potentialHgScript = new File(scriptsDir, FILE_MERCURIAL_SCRIPT);
        File mercurialPackageDir = new File(pythonDir, FILE_LIB_MERCURIAL);
        File templatesDir = new File(mercurialPackageDir, FILE_TEMPLATES);

        if (potentialHgScript.exists() && templatesDir.exists())
        {
          hgScript = potentialHgScript;
        }
      }
    }

    return hgScript;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private String getPythonBinary()
  {
    String python = RegistryUtil.getRegistryValue(REGISTRY_PYTHON[0]);

    if (python == null)
    {
      python = IOUtil.search(new String[0], "python");
    }

    return python;
  }
}
