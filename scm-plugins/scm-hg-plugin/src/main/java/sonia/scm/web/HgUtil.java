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



package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import com.aragost.javahg.Repository;
import com.aragost.javahg.RepositoryConfiguration;

import com.google.common.base.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.SCMContext;
import sonia.scm.repository.HgConfig;
import sonia.scm.repository.HgEnvironment;
import sonia.scm.repository.HgHookManager;
import sonia.scm.repository.HgPythonScript;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.spi.javahg.HgFileviewExtension;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.nio.charset.Charset;

/**
 *
 * @author Sebastian Sdorra
 */
public final class HgUtil
{

  /** Field description */
  public static final String REVISION_TIP = "tip";

  /**
   * the logger for HgUtil
   */
  private static final Logger logger = LoggerFactory.getLogger(HgUtil.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  private HgUtil() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param handler
   * @param hookManager
   * @param directory
   * @param encoding
   * @param pending
   *
   * @return
   */
  public static Repository open(HgRepositoryHandler handler,
    HgHookManager hookManager, File directory, String encoding, boolean pending)
  {
    String enc = encoding;

    if (Strings.isNullOrEmpty(enc))
    {
      enc = handler.getConfig().getEncoding();
    }

    RepositoryConfiguration repoConfiguration = RepositoryConfiguration.DEFAULT;

    HgEnvironment.prepareEnvironment(repoConfiguration.getEnvironment(),
      handler, hookManager);

    repoConfiguration.addExtension(HgFileviewExtension.class);
    repoConfiguration.setEnablePendingChangesets(pending);

    try
    {
      Charset charset = Charset.forName(enc);

      logger.trace("set encoding {} for mercurial", enc);

      repoConfiguration.setEncoding(charset);
    }
    catch (IllegalArgumentException ex)
    {
      logger.error("could not set encoding for mercurial", ex);
    }

    repoConfiguration.setHgBin(handler.getConfig().getHgBinary());

    return Repository.open(repoConfiguration, directory);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param config
   *
   * @return
   */
  public static String getPythonPath(HgConfig config)
  {
    String pythonPath = Util.EMPTY_STRING;

    if (config != null)
    {
      pythonPath = Util.nonNull(config.getPythonPath());
    }

    if (Util.isNotEmpty(pythonPath))
    {
      pythonPath = pythonPath.concat(File.pathSeparator);
    }

    //J-
    pythonPath = pythonPath.concat(
      HgPythonScript.getScriptDirectory(
        SCMContext.getContext()
      ).getAbsolutePath()
    );
    //J+

    return pythonPath;
  }

  /**
   * Method description
   *
   *
   * @param revision
   *
   * @return
   */
  public static String getRevision(String revision)
  {
    return Util.isEmpty(revision)
      ? REVISION_TIP
      : revision;
  }
}
