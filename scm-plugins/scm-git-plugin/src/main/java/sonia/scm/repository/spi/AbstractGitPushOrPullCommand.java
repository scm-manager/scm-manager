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

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.ScmTransportProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.InternalRepositoryException;

import java.io.File;
import java.util.Collection;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class AbstractGitPushOrPullCommand extends AbstractGitCommand
{

  /** Field description */
  private static final String SCHEME = ScmTransportProtocol.NAME + "://";

  /**
   * the logger for AbstractGitPushOrPullCommand
   */
  private static final Logger logger =
    LoggerFactory.getLogger(AbstractGitPushOrPullCommand.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *  @param handler
   * @param context
   */
  protected AbstractGitPushOrPullCommand(GitRepositoryHandler handler, GitContext context)
  {
    super(context);
    this.handler = handler;
  }

  //~--- methods --------------------------------------------------------------

  protected long push(Repository source, String remoteUrl) {
    Git git = Git.wrap(source);
    org.eclipse.jgit.api.PushCommand push = git.push();

    push.setPushAll().setPushTags();
    push.setRemote(remoteUrl);

    long counter = -1;

    try
    {
      Iterable<PushResult> results = push.call();

      if (results != null)
      {
        counter = 0;

        for (PushResult result : results)
        {
          counter += count(git, result);
        }
      }
    }
    catch (Exception ex)
    {
      throw new InternalRepositoryException(repository, "could not execute push/pull command", ex);
    }

    return counter;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   */
  protected sonia.scm.repository.Repository getRemoteRepository(
    RemoteCommandRequest request)
  {
    Preconditions.checkNotNull(request, "request is required");

    sonia.scm.repository.Repository remoteRepository =
      request.getRemoteRepository();

    Preconditions.checkNotNull(remoteRepository,
      "remote repository is required");

    return remoteRepository;
  }

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   */
  protected String getRemoteUrl(RemoteCommandRequest request)
  {
    String url;
    sonia.scm.repository.Repository remRepo = request.getRemoteRepository();

    if (remRepo != null)
    {
      url = getRemoteUrl(remRepo);
    }
    else if (request.getRemoteUrl() != null)
    {
      url = request.getRemoteUrl().toExternalForm();
    }
    else
    {
      throw new IllegalArgumentException("repository or url is required");
    }

    return url;
  }

  /**
   * Method description
   *
   *
   * @param directory
   *
   * @return
   */
  protected String getRemoteUrl(File directory)
  {
    return SCHEME.concat(directory.getAbsolutePath());
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  protected String getRemoteUrl(sonia.scm.repository.Repository repository)
  {
    return getRemoteUrl(handler.getDirectory(repository.getId()));
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param git
   * @param result
   *
   * @return
   */
  private long count(Git git, PushResult result)
  {
    long counter = 0;
    Collection<RemoteRefUpdate> updates = result.getRemoteUpdates();

    for (RemoteRefUpdate update : updates)
    {
      counter += count(git, update);
    }

    return counter;
  }

  /**
   * Method description
   *
   *
   * @param git
   * @param update
   *
   * @return
   */
  private long count(Git git, RemoteRefUpdate update)
  {
    long counter = 0;

    if (GitUtil.isHead(update.getRemoteName()))
    {
      try
      {
        org.eclipse.jgit.api.LogCommand log = git.log();
        ObjectId oldId = update.getExpectedOldObjectId();

        if (GitUtil.isValidObjectId(oldId))
        {
          log.not(oldId);
        }

        ObjectId newId = update.getNewObjectId();

        if (GitUtil.isValidObjectId(newId))
        {
          log.add(newId);
        }

        Iterable<RevCommit> commits = log.call();

        if (commits != null)
        {
          counter += Iterables.size(commits);
        }

        logger.trace("counting {} commits for ref update {}", counter, update);
      }
      catch (Exception ex)
      {
        logger.error("could not count pushed/pulled changesets", ex);
      }
    }
    else
    {
      logger.debug("do not count non branch ref update {}", update);
    }

    return counter;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected GitRepositoryHandler handler;
}
