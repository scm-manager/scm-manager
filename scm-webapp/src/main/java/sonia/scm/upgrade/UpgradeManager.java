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



package sonia.scm.upgrade;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.SCMContext;
import sonia.scm.plugin.PluginVersion;
import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class UpgradeManager
{

  /** the logger for ScmUpgradeHandler */
  private static final Logger logger =
    LoggerFactory.getLogger(UpgradeManager.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  public void doUpgrade()
  {
    File baseDirectory = SCMContext.getContext().getBaseDirectory();
    File configDirectory = new File(baseDirectory, "config");
    File versionFile = new File(configDirectory, "version.txt");

    if (configDirectory.exists())
    {
      boolean writeVersionFile = false;

      String newVersion = SCMContext.getContext().getVersion();

      if (versionFile.exists())
      {

        String oldVersion = getVersionString(versionFile);

        if (!Strings.isNullOrEmpty(oldVersion) &&!oldVersion.equals(newVersion))
        {
          if (!newVersion.equals(oldVersion))
          {
            writeVersionFile = doUpgradesForOldVersion(baseDirectory,
              configDirectory, oldVersion, newVersion);
          }
        }

      }
      else
      {
        writeVersionFile = doUpgradesForOldVersion(baseDirectory,
          configDirectory, "1.1", newVersion);
      }

      if (writeVersionFile)
      {
        writeVersionFile(versionFile);
        logger.info("upgrade to version {} was successful", newVersion);
      }

    }
    else
    {

      // fresh installation
      IOUtil.mkdirs(configDirectory);
      writeVersionFile(versionFile);
    }
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private List<UpgradeHandler> collectUpgradeHandlers()
  {

    List<UpgradeHandler> upgradeHandlers = Lists.newArrayList();

    upgradeHandlers.add(new TimestampUpgradeHandler());
    upgradeHandlers.add(new ClientDateFormatUpgradeHandler());

    // TODO find upgrade handlers on classpath
    return upgradeHandlers;
  }

  /**
   * Method description
   *
   *
   * @param baseDirectory
   * @param configDirectory
   * @param versionString
   * @param oldVersionString
   * @param newVersionString
   *
   * @return
   */
  private boolean doUpgradesForOldVersion(File baseDirectory,
    File configDirectory, String oldVersionString, String newVersionString)
  {
    logger.info("start upgrade from version \"{}\" to \"{}\"",
      oldVersionString, newVersionString);

    boolean writeVersionFile = false;

    try
    {
      PluginVersion oldVersion = PluginVersion.createVersion(oldVersionString);
      PluginVersion newVersion = PluginVersion.createVersion(newVersionString);

      doUpgradesForOldVersion(baseDirectory, configDirectory, oldVersion,
        newVersion);
      writeVersionFile = true;
    }
    catch (Exception ex)
    {
      logger.error("error upgrade failed", ex);
    }

    return writeVersionFile;
  }

  /**
   * Method description
   *
   *
   * @param baseDirectory
   * @param configDirectory
   * @param version
   * @param oldVersion
   * @param newVersion
   */
  private void doUpgradesForOldVersion(File baseDirectory,
    File configDirectory, PluginVersion oldVersion, PluginVersion newVersion)
  {
    List<UpgradeHandler> upgradeHandlers = collectUpgradeHandlers();

    for (UpgradeHandler upgradeHandler : upgradeHandlers)
    {
      logger.trace("call upgrade handler {}", upgradeHandler.getClass());
      upgradeHandler.doUpgrade(baseDirectory, configDirectory, oldVersion,
        newVersion);
    }

  }

  /**
   * Method description
   *
   *
   * @param versionFile
   */
  private void writeVersionFile(File versionFile)
  {
    OutputStream output = null;

    try
    {
      output = new FileOutputStream(versionFile);
      output.write(SCMContext.getContext().getVersion().getBytes());
    }
    catch (IOException ex)
    {
      logger.error("could not write version file", ex);
    }
    finally
    {
      IOUtil.close(output);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param versionFile
   *
   * @return
   */
  private String getVersionString(File versionFile)
  {
    String version = null;

    try
    {
      version = Files.toString(versionFile, Charsets.UTF_8).trim();
    }
    catch (IOException ex)
    {
      logger.error("could not read version file", ex);
    }

    return version;
  }
}
