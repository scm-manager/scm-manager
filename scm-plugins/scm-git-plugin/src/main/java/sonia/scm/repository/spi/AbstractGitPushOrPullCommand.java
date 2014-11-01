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

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.RepositoryException;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.util.Collection;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class AbstractGitPushOrPullCommand extends AbstractGitCommand
{

  /** Field description */
  private static final String SCHEME = "scm://";

  /**
   * the logger for AbstractGitPushOrPullCommand
   */
  private static final Logger logger =
    LoggerFactory.getLogger(AbstractGitPushOrPullCommand.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param handler
   * @param context
   * @param repository
   */
  protected AbstractGitPushOrPullCommand(GitRepositoryHandler handler,
    GitContext context, sonia.scm.repository.Repository repository)
  {
    super(context, repository);
    this.handler = handler;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   * @param source
   * @param remoteUrl
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  protected long push(Repository source, String remoteUrl)
    throws IOException, RepositoryException
  {
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
      throw new RepositoryException("could not execute push/pull command", ex);
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
      throw new IllegalArgumentException("repository or url is requiered");
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
    return getRemoteUrl(handler.getDirectory(repository));
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

    try
    {
      org.eclipse.jgit.api.LogCommand log = git.log();
      ObjectId oldId = update.getExpectedOldObjectId();

      if (oldId != null)
      {
        log.not(oldId);
      }

      ObjectId newId = update.getNewObjectId();

      if (newId != null)
      {
        log.add(newId);

        Iterable<RevCommit> commits = log.call();

        if (commits != null)
        {
          counter += Iterables.size(commits);
        }
      }
      else
      {
        logger.warn("update without new object id");
      }

    }
    catch (Exception ex)
    {
      logger.error("could not count pushed/pulled changesets", ex);
    }

    return counter;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected GitRepositoryHandler handler;
}
