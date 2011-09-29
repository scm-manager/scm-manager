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

import sonia.scm.util.AssertUtil;
import sonia.scm.web.HgUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgChangesetViewer implements ChangesetViewer
{

  /** Field description */
  public static final String ENV_PENDING = "HG_PENDING";

  /** Field description */
  public static final String ENV_REVISION_LIMIT = "SCM_REVISION_LIMIT";

  /** Field description */
  public static final String ENV_REVISION_START = "SCM_REVISION_START";

  /** Field description */
  public static final String RESOURCE_LOG = "/sonia/scm/hglog.py";

  /** the logger for HgChangesetViewer */
  private static final Logger logger =
    LoggerFactory.getLogger(HgChangesetViewer.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param handler
   * @param repositoryDirectory
   * @param changesetPagingResultContext
   */
  public HgChangesetViewer(HgRepositoryHandler handler,
                           File repositoryDirectory,
                           JAXBContext changesetPagingResultContext)
  {
    this.handler = handler;
    this.repositoryDirectory = repositoryDirectory;
    this.changesetPagingResultContext = changesetPagingResultContext;
  }

  /**
   * Constructs ...
   *
   *
   * @param handler
   * @param repository
   * @param changesetPagingResultContext
   */
  public HgChangesetViewer(HgRepositoryHandler handler, Repository repository,
                           JAXBContext changesetPagingResultContext)
  {
    this(handler, handler.getDirectory(repository),
         changesetPagingResultContext);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   *
   *
   * @param start
   * @param max
   *
   * @return
   *
   * @throws IOException
   */
  @Override
  public ChangesetPagingResult getChangesets(int start, int max)
          throws IOException
  {
    return getChangesets(String.valueOf(start), String.valueOf(max), false);
  }

  /**
   * Method description
   *
   *
   * @param startRev
   * @param limit
   * @param pending
   *
   * @return
   *
   * @throws IOException
   */
  public ChangesetPagingResult getChangesets(String startRev, String limit,
          boolean pending)
          throws IOException
  {
    AssertUtil.assertIsNotEmpty(startRev);
    AssertUtil.assertIsNotEmpty(limit);

    if (logger.isDebugEnabled())
    {
      logger.debug("get changesets for repository {}, start: {}, limit: {}",
                   new Object[] { repositoryDirectory.getName(),
                                  startRev, limit });
    }

    Map<String, String> env = new HashMap<String, String>();

    if (pending)
    {
      env.put(ENV_PENDING, repositoryDirectory.getAbsolutePath());
    }

    env.put(ENV_REVISION_START, startRev);
    env.put(ENV_REVISION_LIMIT, limit);

    return HgUtil.getResultFromScript(ChangesetPagingResult.class,
                                      changesetPagingResultContext,
                                      RESOURCE_LOG, handler,
                                      repositoryDirectory, env);
  }

  /**
   * Method description
   *
   *
   * @param startRev
   * @param limit
   *
   * @return
   *
   * @throws IOException
   */
  public ChangesetPagingResult getChangesets(String startRev, String limit)
          throws IOException
  {
    return getChangesets(startRev, limit, false);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private JAXBContext changesetPagingResultContext;

  /** Field description */
  private HgRepositoryHandler handler;

  /** Field description */
  private File repositoryDirectory;
}
