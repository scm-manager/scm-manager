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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.io.Command;
import sonia.scm.io.CommandResult;
import sonia.scm.io.SimpleCommand;
import sonia.scm.util.IOUtil;
import sonia.scm.web.HgWebConfigWriter;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgInitialConfigBuilder
{

  /** Field description */
  public static final String DIRECTORY_REPOSITORY = "repositories";

  /** Field description */
  private static final String[] PATH = new String[]
  {

    // default path
    "/usr/bin",

    // manually installed
    "/usr/local/bin",

    // mac ports
    "/opt/local/bin",

    // opencsw
    "/opt/csw/bin"
  };

  /** the logger for HgInitialConfigBuilder */
  private static final Logger logger =
    LoggerFactory.getLogger(HgInitialConfigBuilder.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param baseDirectory
   */
  public HgInitialConfigBuilder(File baseDirectory)
  {
    this.baseDirectory = baseDirectory;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public HgConfig createInitialConfig()
  {
    File repoDirectory = new File(
                             baseDirectory,
                             DIRECTORY_REPOSITORY.concat(File.separator).concat(
                               HgRepositoryHandler.TYPE_NAME));

    IOUtil.mkdirs(repoDirectory);
    config.setRepositoryDirectory(repoDirectory);
    config.setHgBinary(search("hg"));
    config.setPythonBinary(search("python"));
    try {
      new HgWebConfigWriter(config).write();
    } catch(IOException ioe) {
      if(logger.isErrorEnabled()) {
        logger.error("Could not write Hg CGI for inital config.  " + 
            "HgWeb may not function until a new Hg config is set", ioe);
      }
    }

    return config;
  }

  /**
   * TODO check for windows
   *
   *
   * @param cmd
   *
   * @return
   */
  public static String search(String cmd)
  {
    String cmdPath = null;

    try
    {
      Command command = new SimpleCommand(cmd, "--version");
      CommandResult result = command.execute();

      if (result.isSuccessfull())
      {
        cmdPath = cmd;
      }
    }
    catch (IOException ex) {}

    if (cmdPath == null)
    {
      for (String pathPart : PATH)
      {
        File file = new File(pathPart, cmd);

        if (file.exists())
        {
          cmdPath = file.getAbsolutePath();

          break;
        }
      }
    }

    if (cmdPath != null)
    {
      if (logger.isInfoEnabled())
      {
        logger.info("found {} at {}", cmd, cmdPath);
      }
    }
    else if (logger.isWarnEnabled())
    {
      logger.warn("could not find {}", cmd);
    }

    return cmdPath;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private File baseDirectory;

  /** Field description */
  private HgConfig config = new HgConfig();
}
