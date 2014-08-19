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



package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.io.SVNRepository;

import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.SvnUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class SvnLogCommand extends AbstractSvnCommand implements LogCommand
{

  /**
   * the logger for SvnLogCommand
   */
  private static final Logger logger =
    LoggerFactory.getLogger(SvnLogCommand.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param context
   * @param repository
   * @param repositoryDirectory
   */
  SvnLogCommand(SvnContext context, Repository repository)
  {
    super(context, repository);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param revision
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  @SuppressWarnings("unchecked")
  public Changeset getChangeset(String revision)
    throws IOException, RepositoryException
  {
    Changeset changeset = null;

    if (logger.isDebugEnabled())
    {
      logger.debug("fetch changeset {}", revision);
    }

    try
    {
      long revisioNumber = parseRevision(revision);
      SVNRepository repo = open();
      Collection<SVNLogEntry> entries = repo.log(null, null, revisioNumber,
                                          revisioNumber, true, true);

      if (Util.isNotEmpty(entries))
      {
        changeset = SvnUtil.createChangeset(entries.iterator().next());
      }
    }
    catch (SVNException ex)
    {
      throw new RepositoryException("could not open repository", ex);
    }

    return changeset;
  }

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  @SuppressWarnings("unchecked")
  public ChangesetPagingResult getChangesets(LogCommandRequest request)
    throws IOException, RepositoryException
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("fetch changesets for {}", request);
    }

    ChangesetPagingResult changesets = null;
    int start = request.getPagingStart();
    int limit = request.getPagingLimit();
    long startRevision = parseRevision(request.getStartChangeset());
    long endRevision = parseRevision(request.getEndChangeset());
    String[] pathArray = null;

    if (!Strings.isNullOrEmpty(request.getPath()))
    {
      pathArray = new String[] { request.getPath() };
    }

    try
    {
      SVNRepository repo = open();

      if ((startRevision > 0) || (pathArray != null))
      {
        changesets = getChangesets(repo, startRevision, endRevision, start,
          limit, pathArray);
      }
      else
      {
        changesets = getChangesets(repo, start, limit);
      }
    }
    catch (SVNException ex)
    {
      throw new RepositoryException("could not open repository", ex);
    }

    return changesets;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param v
   *
   * @return
   *
   * @throws RepositoryException
   */
  private long parseRevision(String v) throws RepositoryException
  {
    long result = -1l;

    if (!Strings.isNullOrEmpty(v))
    {
      try
      {
        result = Long.parseLong(v);
      }
      catch (NumberFormatException ex)
      {
        throw new RepositoryException(
          String.format("could not convert revision %s", v), ex);
      }
    }

    return result;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repo
   * @param start
   * @param limit
   *
   * @return
   *
   * @throws SVNException
   */
  private ChangesetPagingResult getChangesets(SVNRepository repo, int start,
    int limit)
    throws SVNException
  {
    long latest = repo.getLatestRevision();
    long startRev = latest - start;
    long endRev = Math.max(startRev - (limit - 1), 0);

    final List<Changeset> changesets = Lists.newArrayList();

    if (startRev > 0)
    {
      logger.debug("fetch changeset from {} to {}", startRev, endRev);
      repo.log(null, startRev, endRev, true, true,
        new ChangesetCollector(changesets));
    }

    return new ChangesetPagingResult((int) (latest + 1l), changesets);
  }

  /**
   * Method description
   *
   *
   * @param repo
   * @param startRevision
   * @param endRevision
   * @param start
   * @param limit
   * @param path
   *
   * @return
   *
   * @throws SVNException
   */
  @SuppressWarnings("unchecked")
  private ChangesetPagingResult getChangesets(SVNRepository repo,
    long startRevision, long endRevision, int start, int limit, String[] path)
    throws SVNException
  {
    long startRev;
    long endRev = Math.max(endRevision, 0);
    long maxRev = repo.getLatestRevision();

    if (startRevision >= 0l)
    {
      startRev = startRevision;
    }
    else
    {
      startRev = maxRev;
    }

    List<SVNLogEntry> changesetList = Lists.newArrayList();

    logger.debug("fetch changeset from {} to {} for path {}", startRev, endRev,
      path);

    Collection<SVNLogEntry> entries = repo.log(path, null, startRev, endRev,
                                        true, true);

    for (SVNLogEntry entry : entries)
    {
      if (entry.getRevision() <= maxRev)
      {
        changesetList.add(entry);
      }
    }

    int total = changesetList.size();
    int max = limit + start;
    int end = total;

    if ((max > 0) && (end > max))
    {
      end = max;
    }

    if (start < 0)
    {
      start = 0;
    }

    logger.trace(
      "create sublist from {} to {} of total {} collected changesets", start,
      end, total);

    changesetList = changesetList.subList(start, end);

    return new ChangesetPagingResult(total,
      SvnUtil.createChangesets(changesetList));
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Collect and convert changesets.
   *
   */
  private static class ChangesetCollector implements ISVNLogEntryHandler
  {

    /**
     * Constructs ...
     *
     *
     * @param changesets
     */
    public ChangesetCollector(Collection<Changeset> changesets)
    {
      this.changesets = changesets;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param logEntry
     *
     * @throws SVNException
     */
    @Override
    public void handleLogEntry(SVNLogEntry logEntry) throws SVNException
    {
      changesets.add(SvnUtil.createChangeset(logEntry));
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private final Collection<Changeset> changesets;
  }
}
