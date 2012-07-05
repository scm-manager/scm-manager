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

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.GitChangesetConverter;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.RepositoryException;
import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class GitLogCommand extends AbstractGitCommand implements LogCommand
{

  /**
   * the logger for GitLogCommand
   */
  private static final Logger logger =
    LoggerFactory.getLogger(GitLogCommand.class);

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
  GitLogCommand(GitContext context, sonia.scm.repository.Repository repository)
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
   */
  @Override
  public Changeset getChangeset(String revision)
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("fetch changeset {}", revision);
    }

    Changeset changeset = null;
    Repository gr = null;
    GitChangesetConverter converter = null;

    try
    {
      gr = open();

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
   * @param request
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public ChangesetPagingResult getChangesets(LogCommandRequest request)
    throws IOException, RepositoryException
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("fetch changesets for request: {}", request);
    }

    ChangesetPagingResult changesets = null;
    GitChangesetConverter converter = null;

    try
    {
      org.eclipse.jgit.lib.Repository gr = open();

      if (!gr.getAllRefs().isEmpty())
      {
        int counter = 0;
        int start = request.getPagingStart();

        if (start < 0)
        {
          if (logger.isErrorEnabled())
          {
            logger.error("start parameter is negative, reset to 0");
          }

          start = 0;
        }

        List<Changeset> changesetList = Lists.newArrayList();
        int limit = request.getPagingLimit();
        ObjectId startId = null;

        if (!Strings.isNullOrEmpty(request.getStartChangeset()))
        {
          startId = gr.resolve(request.getStartChangeset());
        }

        ObjectId endId = null;

        if (!Strings.isNullOrEmpty(request.getEndChangeset()))
        {
          endId = gr.resolve(request.getEndChangeset());
        }

        RevWalk revWalk = new RevWalk(gr);

        converter = new GitChangesetConverter(gr, revWalk, GitUtil.ID_LENGTH);

        if (!Strings.isNullOrEmpty(request.getPath()))
        {
          revWalk.setTreeFilter(
            AndTreeFilter.create(
              PathFilter.create(request.getPath()), TreeFilter.ANY_DIFF));
        }

        ObjectId head = GitUtil.getRepositoryHead(gr);

        if (head != null)
        {
          if (startId != null)
          {
            revWalk.markStart(revWalk.lookupCommit(startId));
          }
          else
          {
            revWalk.markStart(revWalk.lookupCommit(head));
          }

          Iterator<RevCommit> iterator = revWalk.iterator();

          while (iterator.hasNext())
          {
            RevCommit commit = iterator.next();

            if ((counter >= start)
              && ((limit < 0) || (counter < start + limit)))
            {
              changesetList.add(converter.createChangeset(commit));
            }

            counter++;

            if ((endId != null) && commit.getId().equals(endId))
            {
              break;
            }
          }
        }

        changesets = new ChangesetPagingResult(counter, changesetList);
      }
      else if (logger.isWarnEnabled())
      {
        logger.warn("the repository {} seems to be empty",
          repository.getName());
      }
    }
    catch (Exception ex)
    {
      throw new RepositoryException("could not create change log", ex);
    }
    finally
    {
      IOUtil.close(converter);
    }

    return changesets;
  }
}
