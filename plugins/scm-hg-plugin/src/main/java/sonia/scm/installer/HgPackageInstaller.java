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

import sonia.scm.io.ZipUnArchiver;
import sonia.scm.repository.HgConfig;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.URL;

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
   * @param handler
   * @param baseDirectory
   * @param pkg
   */
  public HgPackageInstaller(HgRepositoryHandler handler, File baseDirectory,
                            HgPackage pkg)
  {
    this.handler = handler;
    this.baseDirectory = baseDirectory;
    this.pkg = pkg;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  public void run()
  {
    File downloadedFile = downloadFile();

    if ((downloadedFile != null) && downloadedFile.exists())
    {
      File directory = extractPackage(downloadedFile);

      updateConfig(directory);
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

      input = new URL(pkg.getUrl()).openStream();
      output = new FileOutputStream(file);
      IOUtil.copy(input, output);
    }
    catch (IOException ex)
    {
      logger.error("could not downlaod file ".concat(pkg.getUrl()), ex);
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

    config.setHgBinary(MessageFormat.format(template.getHgBinary(), path));
    config.setPythonBinary(MessageFormat.format(template.getPythonBinary(),
            path));
    config.setPythonPath(MessageFormat.format(template.getPythonPath(), path));
    config.setUseOptimizedBytecode(template.isUseOptimizedBytecode());
    handler.storeConfig();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private File baseDirectory;

  /** Field description */
  private HgRepositoryHandler handler;

  /** Field description */
  private HgPackage pkg;
}
