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

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.IOUtil;

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
public class GitRepositoryHookEvent extends AbstractRepositoryHookEvent
{

  /** the logger for GitRepositoryHookEvent */
  private static final Logger logger =
    LoggerFactory.getLogger(GitRepositoryHookEvent.class);

  //~--- get methods ----------------------------------------------------------

  public GitRepositoryHookEvent(File directory,
    ObjectId newId, ObjectId oldId)
  {
    this.directory = directory;
    this.newId = newId;
    this.oldId = oldId;
  }

  
  
  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<Changeset> getChangesets()
  {
    if (changesets == null)
    {
      changesets = fetchChangesets();
    }

    return changesets;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public RepositoryHookType getType()
  {
    return RepositoryHookType.POST_RECEIVE;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  private List<Changeset> fetchChangesets()
  {
    List<Changeset> result = new ArrayList<Changeset>();

    if (newId != null)
    {
      GitChangesetConverter converter = null;
      RevWalk walk = null;
      org.eclipse.jgit.lib.Repository repository = null;

      try
      {
        repository = GitUtil.open(directory);
        converter = new GitChangesetConverter(repository, GitUtil.ID_LENGTH);
        walk = new RevWalk(repository);
        walk.reset();
        walk.sort(RevSort.NONE);
        walk.markStart(walk.parseCommit(newId));

        if (oldId != null)
        {
          walk.markUninteresting(walk.parseCommit(oldId));
        }

        RevCommit commit = walk.next();

        while (commit != null)
        {
          result.add(converter.createChangeset(commit));
          commit = walk.next();
        }
      }
      catch (IOException ex)
      {
        logger.error("could not fetch changesets", ex);
      }
      finally
      {
        IOUtil.close(converter);
        GitUtil.release(walk);
        GitUtil.close(repository);
      }
    }

    return result;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private List<Changeset> changesets;

  /** Field description */
  private File directory;

  /** Field description */
  private ObjectId newId;

  /** Field description */
  private ObjectId oldId;
}
