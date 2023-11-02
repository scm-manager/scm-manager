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
import com.google.inject.assistedinject.Assisted;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.GitChangesetConverter;
import sonia.scm.repository.GitChangesetConverterFactory;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.util.IOUtil;

import javax.inject.Inject;
import java.io.IOException;

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
  GitLogCommand(@Assisted GitContext context, GitChangesetConverterFactory converterFactory)
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
  @SuppressWarnings("java:S2093")
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
      logger.error("could not open repository: " + repository.getNamespaceAndName(), ex);
    }
    catch (NullPointerException e)
    {
      throw notFound(entity(REVISION, revision).in(this.repository));
    }
    finally
    {
      IOUtil.close(converter);
      GitUtil.release(revWalk);
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
  @SuppressWarnings("java:S2093")
  public ChangesetPagingResult getChangesets(LogCommandRequest request) {
    try {
      if (Strings.isNullOrEmpty(request.getBranch())) {
        request.setBranch(context.getConfig().getDefaultBranch());
      }
      return new GitLogComputer(this.repository.getId(), open(), converterFactory).compute(request);
    } catch (IOException e) {
      throw new InternalRepositoryException(repository, "could not create change log", e);
    }
  }

  public interface Factory {
    LogCommand create(GitContext context);
  }
}
