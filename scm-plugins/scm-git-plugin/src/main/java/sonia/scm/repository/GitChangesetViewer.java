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

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class GitChangesetViewer implements ChangesetViewer
{

  /** the logger for GitChangesetViewer */
  private static final Logger logger =
    LoggerFactory.getLogger(GitChangesetViewer.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param handler
   * @param repository
   */
  public GitChangesetViewer(GitRepositoryHandler handler, Repository repository)
  {
    this.handler = handler;
    this.repository = repository;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param revision
   *
   * @return
   */
  @Override
  public Changeset getChangeset(String revision)
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("fetch changeset {}", revision);
    }

    Changeset changeset = null;
    File directory = handler.getDirectory(repository);
    org.eclipse.jgit.lib.Repository gr = null;
    GitChangesetConverter converter = null;

    try
    {
      gr = GitUtil.open(directory);

      if (!gr.getAllRefs().isEmpty())
      {
        RevWalk revWalk = new RevWalk(gr);
        ObjectId id = GitUtil.getRevisionId(gr, revision);
        RevCommit commit = revWalk.parseCommit(id);

        if (commit != null)
        {
          converter = new GitChangesetConverter(gr, revWalk, GitUtil.ID_LENGTH);
          changeset = converter.createChangeset(commit);
        }
        else if (logger.isWarnEnabled())
        {
          logger.warn("could not find revision {}", revision);
        }
      }
    }
    catch (IOException ex)
    {
      logger.error("could not open repository", ex);
    }
    finally
    {
      IOUtil.close(converter);
      GitUtil.close(gr);
    }

    return changeset;
  }

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
    if (logger.isDebugEnabled())
    {
      logger.debug("fetch changesets. start: {}, max: {}", start, max);
    }

    ChangesetPagingResult changesets = null;
    File directory = handler.getDirectory(repository);
    org.eclipse.jgit.lib.Repository gr = null;
    GitChangesetConverter converter = null;

    try
    {
      gr = GitUtil.open(directory);

      int counter = 0;
      List<Changeset> changesetList = new ArrayList<Changeset>();

      if (!gr.getAllRefs().isEmpty())
      {
        converter = new GitChangesetConverter(gr, GitUtil.ID_LENGTH);

        Git git = new Git(gr);
        ObjectId headId = GitUtil.getRepositoryHead(gr);

        if (headId != null)
        {
          for (RevCommit commit : git.log().add(headId).call())
          {
            if ((counter >= start) && (counter < start + max))
            {
              changesetList.add(converter.createChangeset(commit));
            }

            counter++;
          }
        }
        else if (logger.isWarnEnabled())
        {
          logger.warn("could not find repository head of repository {}",
                      repository.getName());
        }
      }

      changesets = new ChangesetPagingResult(counter, changesetList);
    }
    catch (NoHeadException ex)
    {
      logger.error("could not read changesets", ex);
    }
    catch (IOException ex)
    {
      logger.error("could not open repository", ex);
    }
    finally
    {
      IOUtil.close(converter);
      GitUtil.close(gr);
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
    if (logger.isDebugEnabled())
    {
      logger.debug(
          "fetch changesets for path {} and revision {}. start: {}, max: {}",
          new Object[] { path,
                         revision, start, max });
    }

    ChangesetPagingResult changesets = null;
    File directory = handler.getDirectory(repository);
    org.eclipse.jgit.lib.Repository gr = null;
    GitChangesetConverter converter = null;

    try
    {
      gr = GitUtil.open(directory);

      int counter = 0;
      List<Changeset> changesetList = new ArrayList<Changeset>();

      if (!gr.getAllRefs().isEmpty())
      {
        converter = new GitChangesetConverter(gr, GitUtil.ID_LENGTH);

        Git git = new Git(gr);
        ObjectId revisionId = GitUtil.getRevisionId(gr, revision);

        if (revisionId != null)
        {
          for (RevCommit commit :
                  git.log().add(revisionId).addPath(path).call())
          {
            if ((counter >= start) && (counter < start + max))
            {
              changesetList.add(converter.createChangeset(commit));
            }

            counter++;
          }
        }
        else if (logger.isWarnEnabled())
        {
          logger.warn("could not find repository head of repository {}",
                      repository.getName());
        }
      }

      changesets = new ChangesetPagingResult(counter, changesetList);
    }
    catch (NoHeadException ex)
    {
      logger.error("could not read changesets", ex);
    }
    catch (IOException ex)
    {
      logger.error("could not open repository", ex);
    }
    finally
    {
      IOUtil.close(converter);
      GitUtil.close(gr);
    }

    return changesets;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private GitRepositoryHandler handler;

  /** Field description */
  private Repository repository;
}
