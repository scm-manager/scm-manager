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
    super.install(baseDirectory, config);

    String pythonBinary = getPythonBinary();

    config.setPythonBinary(pythonBinary);

    File hgScript = getMercurialScript(pythonBinary);
    File hgDirectory = getMercurialDirectory();

    if (hgScript != null)
    {
      config.setHgBinary(hgScript.getAbsolutePath());
    }
    else if (hgDirectory != null)
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

    config.setHgBinary(new File(hgDirectory,
                                FILE_MERCURIAL_EXE).getAbsolutePath());
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
        File file = new File(path);

        if (!file.exists())
        {
          installations.add(path);
        }
      }
    }

    return installations;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private File getMercurialDirectory()
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
