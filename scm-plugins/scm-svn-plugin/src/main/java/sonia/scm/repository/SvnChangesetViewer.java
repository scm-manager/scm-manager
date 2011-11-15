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

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class SvnChangesetViewer implements ChangesetViewer
{

  /** the logger for SvnChangesetViewer */
  private static final Logger logger =
    LoggerFactory.getLogger(SvnChangesetViewer.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param handler
   * @param repostory
   */
  public SvnChangesetViewer(SvnRepositoryHandler handler, Repository repostory)
  {
    this.handler = handler;
    this.repostory = repostory;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param start
   * @param max
   *
   * @return
   */
  @Override
  public ChangesetPagingResult getChangesets(int start, int max)
  {
    ChangesetPagingResult changesets = null;
    File directory = handler.getDirectory(repostory);
    SVNRepository repository = null;

    try
    {
      repository = SVNRepositoryFactory.create(SVNURL.fromFile(directory));

      long total = repository.getLatestRevision();
      long startRev = total - start;
      long endRev = total - start - (max - 1);

      if (endRev < 0)
      {
        endRev = 0;
      }

      List<Changeset> changesetList = new ArrayList<Changeset>();
      Collection<SVNLogEntry> entries = repository.log(null, null, startRev,
                                          endRev, true, true);

      for (SVNLogEntry entry : entries)
      {
        changesetList.add(SvnUtil.createChangeset(entry));
      }

      total++;
      changesets = new ChangesetPagingResult((int) total, changesetList);
    }
    catch (SVNException ex)
    {
      logger.error("could not open repository", ex);
    }
    finally
    {
      repository.closeSession();
    }

    return changesets;
  }

  /**
   * Method description
   *
   *
   * @param path
   * @param revision
   * @param start
   * @param max
   *
   * @return
   */
  @Override
  public ChangesetPagingResult getChangesets(String path, String revision,
          int start, int max)
  {
    ChangesetPagingResult changesets = null;
    File directory = handler.getDirectory(repostory);
    SVNRepository repository = null;

    try
    {
      repository = SVNRepositoryFactory.create(SVNURL.fromFile(directory));

      long startRev = repository.getLatestRevision();
      long endRev = 0;
      long maxRev = startRev;

      if (Util.isNotEmpty(revision))
      {
        try
        {
          maxRev = Long.parseLong(revision);
        }
        catch (NumberFormatException ex)
        {
          logger.error("could not parse revision ".concat(revision), ex);
        }
      }

      List<Changeset> changesetList = new ArrayList<Changeset>();
      Collection<SVNLogEntry> entries = repository.log(new String[] { path },
                                          null, startRev, endRev, true, true);

      for (SVNLogEntry entry : entries)
      {
        if (entry.getRevision() <= maxRev)
        {
          changesetList.add(SvnUtil.createChangeset(entry));
        }
      }

      int total = changesetList.size();
      int end = total - start;

      if (end > max)
      {
        end = max;
      }

      if (start < 0)
      {
        start = 0;
      }

      changesetList = changesetList.subList(start, end);
      changesets = new ChangesetPagingResult(total, changesetList);
    }
    catch (SVNException ex)
    {
      logger.error("could not open repository", ex);
    }
    finally
    {
      repository.closeSession();
    }

    return changesets;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private SvnRepositoryHandler handler;

  /** Field description */
  private Repository repostory;
}
