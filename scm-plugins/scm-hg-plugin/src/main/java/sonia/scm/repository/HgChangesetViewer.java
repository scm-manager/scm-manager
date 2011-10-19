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

import sonia.scm.util.Util;

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
public class HgChangesetViewer extends AbstractHgHandler
        implements ChangesetViewer
{

  /** Field description */
  public static final String RESOURCE_LOG = "/sonia/scm/hglog.py";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param handler
   * @param changesetPagingResultContext
   * @param context
   * @param repositoryDirectory
   */
  public HgChangesetViewer(HgRepositoryHandler handler,
                           JAXBContext changesetPagingResultContext,
                           HgContext context, File repositoryDirectory)
  {
    super(handler, changesetPagingResultContext, context, repositoryDirectory);
  }

  /**
   * Constructs ...
   *
   *
   *
   * @param handler
   * @param context
   * @param changesetPagingResultContext
   * @param repository
   */
  public HgChangesetViewer(HgRepositoryHandler handler,
                           JAXBContext changesetPagingResultContext,
                           HgContext context, Repository repository)
  {
    super(handler, changesetPagingResultContext, context, repository);
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
   * @throws RepositoryException
   */
  @Override
  public ChangesetPagingResult getChangesets(int start, int max)
          throws IOException, RepositoryException
  {
    return getChangesets(String.valueOf(start), String.valueOf(max), null,
                         null);
  }

  /**
   * Method description
   *
   *
   * @param pageStart
   * @param pageLimit
   * @param revisionStart
   * @param revisionEnd
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  public ChangesetPagingResult getChangesets(String pageStart,
          String pageLimit, String revisionStart, String revisionEnd)
          throws IOException, RepositoryException
  {
    Map<String, String> env = new HashMap<String, String>();

    env.put(ENV_PAGE_START, Util.nonNull(pageStart));
    env.put(ENV_PAGE_LIMIT, Util.nonNull(pageLimit));
    env.put(ENV_REVISION_START, Util.nonNull(revisionStart));
    env.put(ENV_REVISION_END, Util.nonNull(revisionEnd));

    return getResultFromScript(ChangesetPagingResult.class, RESOURCE_LOG, env);
  }

  /**
   * Method description
   *
   *
   * @param startNode
   * @param endNode
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  public ChangesetPagingResult getChangesets(String startNode, String endNode)
          throws IOException, RepositoryException
  {
    return getChangesets(null, null, startNode, endNode);
  }
}
