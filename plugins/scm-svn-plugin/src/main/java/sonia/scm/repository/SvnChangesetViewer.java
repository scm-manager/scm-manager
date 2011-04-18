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
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
      Collection<SVNLogEntry> entries = repository.log(new String[] { "" },
                                          null, startRev, endRev, true, true);

      for (SVNLogEntry entry : entries)
      {
        changesetList.add(createChangeset(entry));
      }

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

  //~--- methods --------------------------------------------------------------

  /**
   * TODO: type replaced
   *
   *
   * @param modifications
   * @param entry
   */
  private void appendModification(Modifications modifications,
                                  SVNLogEntryPath entry)
  {
    switch (entry.getType())
    {
      case SVNLogEntryPath.TYPE_ADDED :
        modifications.getAdded().add(entry.getPath());

        break;

      case SVNLogEntryPath.TYPE_DELETED :
        modifications.getRemoved().add(entry.getPath());

        break;

      case SVNLogEntryPath.TYPE_MODIFIED :
        modifications.getModified().add(entry.getPath());

        break;
    }
  }

  /**
   * Method description
   *
   *
   * @param entry
   *
   * @return
   */
  @SuppressWarnings("unchecked")
  private Changeset createChangeset(SVNLogEntry entry)
  {
    Changeset changeset = new Changeset(String.valueOf(entry.getRevision()),
                            entry.getDate().getTime(),
                            Person.toPerson(entry.getAuthor()),
                            entry.getMessage());
    Map<String, SVNLogEntryPath> changeMap = entry.getChangedPaths();

    if (Util.isNotEmpty(changeMap))
    {
      Modifications modifications = changeset.getModifications();

      for (SVNLogEntryPath e : changeMap.values())
      {
        appendModification(modifications, e);
      }
    }

    return changeset;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private SvnRepositoryHandler handler;

  /** Field description */
  private Repository repostory;
}
