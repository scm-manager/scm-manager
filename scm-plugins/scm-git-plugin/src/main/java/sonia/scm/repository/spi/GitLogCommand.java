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
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.NotFoundException;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.GitChangesetConverter;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.util.IOUtil;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

//~--- JDK imports ------------------------------------------------------------

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
  public static final String REVISION = "Revision";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param context
   * @param repository
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
    RevWalk revWalk = null;

    try
    {
      gr = open();

      if (!gr.getAllRefs().isEmpty())
      {
        revWalk = new RevWalk(gr);
        ObjectId id = GitUtil.getRevisionId(gr, revision);
        RevCommit commit = revWalk.parseCommit(id);

        if (commit != null)
        {
          converter = new GitChangesetConverter(gr, revWalk);
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
    catch (NullPointerException e)
    {
      throw notFound(entity(REVISION, revision).in(this.repository));
    }
    finally
    {
      IOUtil.close(converter);
      GitUtil.release(revWalk);
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
   */
  @Override
  @SuppressWarnings("unchecked")
  public ChangesetPagingResult getChangesets(LogCommandRequest request) {
    if (logger.isDebugEnabled()) {
      logger.debug("fetch changesets for request: {}", request);
    }

    ChangesetPagingResult changesets = null;
    GitChangesetConverter converter = null;
    RevWalk revWalk = null;

    try (org.eclipse.jgit.lib.Repository repository = open()) {
      if (!repository.getAllRefs().isEmpty()) {
        int counter = 0;
        int start = request.getPagingStart();

        if (start < 0) {
          if (logger.isErrorEnabled()) {
            logger.error("start parameter is negative, reset to 0");
          }

          start = 0;
        }

        List<Changeset> changesetList = Lists.newArrayList();
        int limit = request.getPagingLimit();
        ObjectId startId = null;

        if (!Strings.isNullOrEmpty(request.getStartChangeset())) {
          startId = repository.resolve(request.getStartChangeset());
        }

        ObjectId endId = null;

        if (!Strings.isNullOrEmpty(request.getEndChangeset())) {
          endId = repository.resolve(request.getEndChangeset());
        }

        Ref branch = getBranchOrDefault(repository,request.getBranch());

        ObjectId ancestorId = null;

        if (!Strings.isNullOrEmpty(request.getAncestorChangeset())) {
          ancestorId = repository.resolve(request.getAncestorChangeset());
          if (ancestorId == null) {
            throw notFound(entity(REVISION, request.getAncestorChangeset()).in(this.repository));
          }
        }

        revWalk = new RevWalk(repository);

        converter = new GitChangesetConverter(repository, revWalk);

        if (!Strings.isNullOrEmpty(request.getPath())) {
          revWalk.setTreeFilter(
            AndTreeFilter.create(
              PathFilter.create(request.getPath()), TreeFilter.ANY_DIFF));
        }

        if (branch != null) {
          if (startId != null) {
            revWalk.markStart(revWalk.lookupCommit(startId));
          } else {
            revWalk.markStart(revWalk.lookupCommit(branch.getObjectId()));
          }

          if (ancestorId != null) {
            revWalk.markUninteresting(revWalk.lookupCommit(ancestorId));
          }

          Iterator<RevCommit> iterator = revWalk.iterator();

          while (iterator.hasNext()) {
            RevCommit commit = iterator.next();

            if ((counter >= start)
              && ((limit < 0) || (counter < start + limit))) {
              changesetList.add(converter.createChangeset(commit));
            }

            counter++;

            if (commit.getId().equals(endId)) {
              break;
            }
          }
        } else if (ancestorId != null) {
          throw notFound(entity(REVISION, request.getBranch()).in(this.repository));
        }

        if (branch != null) {
          changesets = new ChangesetPagingResult(counter, changesetList, GitUtil.getBranch(branch.getName()));
        } else {
          changesets = new ChangesetPagingResult(counter, changesetList);
        }
      } else if (logger.isWarnEnabled()) {
        logger.warn("the repository {} seems to be empty",
          this.repository.getName());

        changesets = new ChangesetPagingResult(0, Collections.EMPTY_LIST);
      }
    }
    catch (MissingObjectException e)
    {
      throw notFound(entity(REVISION, e.getObjectId().getName()).in(repository));
    }
    catch (NotFoundException e)
    {
      throw e;
    }
    catch (Exception ex)
    {
      throw new InternalRepositoryException(repository, "could not create change log", ex);
    }
    finally
    {
      IOUtil.close(converter);
      GitUtil.release(revWalk);
    }

    return changesets;
  }
}
