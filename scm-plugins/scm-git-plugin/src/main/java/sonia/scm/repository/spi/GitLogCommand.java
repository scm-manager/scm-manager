/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
import sonia.scm.repository.GitChangesetConverterFactory;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.util.IOUtil;

import javax.inject.Inject;
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
  private final GitChangesetConverterFactory converterFactory;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *  @param context
   *
   */
  @Inject
  GitLogCommand(GitContext context, GitChangesetConverterFactory converterFactory)
  {
    super(context);
    this.converterFactory = converterFactory;
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
  public Changeset getChangeset(String revision, LogCommandRequest request)
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
          converter = converterFactory.create(gr, revWalk);

          if (isBranchRequested(request)) {
            String branch = request.getBranch();
            if (isMergedIntoBranch(gr, revWalk, commit, branch)) {
              logger.trace("returning commit {} with branch {}", commit.getId(), branch);
              changeset = converter.createChangeset(commit, branch);
            } else {
              logger.debug("returning null, because commit {} was not merged into branch {}", commit.getId(), branch);
            }
          } else {
            changeset = converter.createChangeset(commit);
          }
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

  private boolean isMergedIntoBranch(Repository repository, RevWalk revWalk, RevCommit commit, String branchName) throws IOException {
    return revWalk.isMergedInto(commit, findHeadCommitOfBranch(repository, revWalk, branchName));
  }

  private boolean isBranchRequested(LogCommandRequest request) {
    return request != null && !Strings.isNullOrEmpty(request.getBranch());
  }

  private RevCommit findHeadCommitOfBranch(Repository repository, RevWalk revWalk, String branchName) throws IOException {
    return revWalk.parseCommit(GitUtil.getCommit(repository, revWalk, repository.findRef(branchName)));
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

        converter = converterFactory.create(repository, revWalk);

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
