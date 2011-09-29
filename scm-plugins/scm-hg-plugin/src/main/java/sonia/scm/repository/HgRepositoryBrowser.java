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

import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;
import sonia.scm.web.HgUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgRepositoryBrowser implements RepositoryBrowser
{

  /** Field description */
  public static final String DEFAULT_REVISION = "tip";

  /** Field description */
  public static final String ENV_PATH = "SCM_PATH";

  /** Field description */
  public static final String ENV_PYTHON_PATH = "SCM_PYTHON_PATH";

  /** Field description */
  public static final String ENV_REPOSITORY_PATH = "SCM_REPOSITORY_PATH";

  /** Field description */
  public static final String ENV_REVISION = "SCM_REVISION";

  /** Field description */
  public static final String RESOURCE_BROWSE = "/sonia/scm/hgbrowse.py";

  /** the logger for HgRepositoryBrowser */
  private static final Logger logger =
    LoggerFactory.getLogger(HgRepositoryBrowser.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param handler
   * @param repository
   * @param browserResultContext
   */
  public HgRepositoryBrowser(HgRepositoryHandler handler,
                             Repository repository,
                             JAXBContext browserResultContext)
  {
    this.handler = handler;
    this.repository = repository;
    this.browserResultContext = browserResultContext;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param revision
   * @param path
   * @param output
   *
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public void getContent(String revision, String path, OutputStream output)
          throws IOException, RepositoryException
  {
    if (Util.isEmpty(revision))
    {
      revision = DEFAULT_REVISION;
    }

    File directory = handler.getDirectory(repository);
    ProcessBuilder builder =
      new ProcessBuilder(handler.getConfig().getHgBinary(), "cat", "-r",
                         revision, Util.nonNull(path));

    if (logger.isDebugEnabled())
    {
      StringBuilder msg = new StringBuilder();

      for (String param : builder.command())
      {
        msg.append(param).append(" ");
      }

      logger.debug(msg.toString());
    }

    Process p = builder.directory(directory).start();
    InputStream input = null;

    try
    {
      input = p.getInputStream();
      IOUtil.copy(input, output);
    }
    finally
    {
      IOUtil.close(input);
    }
  }

  /**
   * Method description
   *
   *
   * @param revision
   * @param path
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public BrowserResult getResult(String revision, String path)
          throws IOException, RepositoryException
  {
    return HgUtil.getResultFromScript(BrowserResult.class,
                                      browserResultContext, RESOURCE_BROWSE,
                                      handler, repository, revision, path);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private JAXBContext browserResultContext;

  /** Field description */
  private HgRepositoryHandler handler;

  /** Field description */
  private Repository repository;
}
