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
import sonia.scm.util.HttpUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.function.Consumer;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Sebastian Sdorra
 */
public final class HgUtil
{

  /** Field description */
  public static final String REVISION_TIP = "tip";

  /** Field description */
  private static final String USERAGENT_HG = "mercurial/";

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
    return open(
      handler,
      directory,
      encoding,
      pending,
      environment -> HgEnvironment.prepareEnvironment(environment, handler, hookManager)
    );
  }

  public static Repository open(HgRepositoryHandler handler,
                                File directory, String encoding, boolean pending,
                                Consumer<Map<String, String>> prepareEnvironment)
  {
    String enc = encoding;

    if (Strings.isNullOrEmpty(enc))
    {
      enc = handler.getConfig().getEncoding();
    }

    RepositoryConfiguration repoConfiguration = RepositoryConfiguration.DEFAULT;

    prepareEnvironment.accept(repoConfiguration.getEnvironment());

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

    logger.debug("open hg repository {}: encoding: {}, pending: {}", directory, enc, pending);

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

  /**
   * Returns true if the request comes from a mercurial client.
   *
   *
   * @param request servlet request
   *
   * @return true if the client is mercurial
   */
  public static boolean isHgClient(HttpServletRequest request)
  {
    return HttpUtil.userAgentStartsWith(request, USERAGENT_HG);
  }
}
