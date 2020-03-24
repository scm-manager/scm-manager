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

import sonia.scm.SCMContext;
import sonia.scm.io.ZipUnArchiver;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.repository.HgConfig;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.HgWindowsPackageFix;
import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.text.MessageFormat;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgPackageInstaller implements Runnable
{

  /** the logger for HgPackageInstaller */
  private static final Logger logger =
    LoggerFactory.getLogger(HgPackageInstaller.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   *
   * @param client
   * @param handler
   * @param baseDirectory
   * @param pkg
   */
  public HgPackageInstaller(AdvancedHttpClient client,
    HgRepositoryHandler handler, File baseDirectory, HgPackage pkg)
  {
    this.client = client;
    this.handler = handler;
    this.baseDirectory = baseDirectory;
    this.pkg = pkg;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean install()
  {
    boolean success = false;
    File downloadedFile = downloadFile();

    if ((downloadedFile != null) && downloadedFile.exists())
    {
      File directory = extractPackage(downloadedFile);

      if ((directory != null) && directory.exists())
      {
        updateConfig(directory);
        success = true;
      }
    }

    return success;
  }

  /**
   * Method description
   *
   */
  @Override
  public void run()
  {
    if (!install())
    {
      logger.error("installation of pkg {} failed", pkg.getId());
    }
    else if (logger.isInfoEnabled())
    {
      logger.info("successfully installed pkg {}", pkg.getId());
    }
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private File downloadFile()
  {
    File file = null;
    InputStream input = null;
    OutputStream output = null;

    try
    {
      file = File.createTempFile("scm-hg-", ".pkg");

      if (logger.isDebugEnabled())
      {
        logger.debug("download package to {}", file.getAbsolutePath());
      }

      // TODO error handling
      input = client.get(pkg.getUrl()).request().contentAsStream();
      output = new FileOutputStream(file);
      IOUtil.copy(input, output);
    }
    catch (IOException ex)
    {
      logger.error("could not downlaod file ".concat(pkg.getUrl()), ex);
      file = null;
    }
    finally
    {
      IOUtil.close(input);
      IOUtil.close(output);
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
  private File extractPackage(File file)
  {
    File directory = new File(baseDirectory,
                       "pkg".concat(File.separator).concat(pkg.getId()));

    IOUtil.mkdirs(directory);

    try
    {
      IOUtil.extract(file, directory, ZipUnArchiver.EXTENSION);
    }
    catch (IOException ex)
    {
      directory = null;
      logger.error("could not extract pacakge ".concat(pkg.getId()), ex);
    }
    finally
    {

      // delete temp file
      try
      {
        IOUtil.delete(file, true);
      }
      catch (IOException ex)
      {
        logger.error(ex.getMessage(), ex);
      }
    }

    return directory;
  }

  /**
   * Method description
   *
   *
   * @param directory
   */
  private void updateConfig(File directory)
  {
    String path = directory.getAbsolutePath();
    HgConfig template = pkg.getHgConfigTemplate();
    HgConfig config = handler.getConfig();

    config.setHgBinary(getTemplateValue(template.getHgBinary(), path));
    config.setPythonBinary(getTemplateValue(template.getPythonBinary(), path));
    config.setPythonPath(getTemplateValue(template.getPythonPath(), path));
    config.setUseOptimizedBytecode(template.isUseOptimizedBytecode());

    // fix wrong hg.bat
    HgWindowsPackageFix.fixHgPackage(SCMContext.getContext(), config);

    handler.storeConfig();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param template
   * @param path
   *
   * @return
   */
  private String getTemplateValue(String template, String path)
  {
    String result = null;

    if (template != null)
    {
      result = MessageFormat.format(template, path);
    }

    return result;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private File baseDirectory;

  /** Field description */
  private AdvancedHttpClient client;

  /** Field description */
  private HgRepositoryHandler handler;

  /** Field description */
  private HgPackage pkg;
}
