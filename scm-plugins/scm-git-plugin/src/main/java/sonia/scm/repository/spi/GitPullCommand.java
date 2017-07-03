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
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.TagOpt;
import org.eclipse.jgit.transport.TrackingRefUpdate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.api.PullResponse;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import java.net.URL;

/**
 *
 * @author Sebastian Sdorra
 */
public class GitPullCommand extends AbstractGitPushOrPullCommand
  implements PullCommand
{

  /** Field description */
  private static final String REF_SPEC = "refs/heads/*:refs/heads/*";

  /** Field description */
  private static final Logger logger =
    LoggerFactory.getLogger(GitPullCommand.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param handler
   * @param context
   * @param repository
   */
  public GitPullCommand(GitRepositoryHandler handler, GitContext context,
    Repository repository)
  {
    super(handler, context, repository);
  }

  //~--- methods --------------------------------------------------------------

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
  public PullResponse pull(PullCommandRequest request)
    throws IOException, RepositoryException
  {
    PullResponse response;
    Repository sourceRepository = request.getRemoteRepository();

    if (sourceRepository != null)
    {
      response = pullFromScmRepository(sourceRepository);
    }
    else if (request.getRemoteUrl() != null)
    {
      response = pullFromUrl(request.getRemoteUrl());
    }
    else
    {
      throw new IllegalArgumentException("repository or url is required");
    }

    return response;
  }

  /**
   * Method description
   *
   *
   * @param git
   * @param result
   * @param fetch
   *
   * @return
   *
   * @throws RepositoryException
   */
  private PullResponse convert(Git git, FetchResult fetch)
    throws RepositoryException
  {
    long counter = 0L;

    for (TrackingRefUpdate tru : fetch.getTrackingRefUpdates())
    {
      counter += count(git, tru);
    }

    logger.debug("received {} changesets by pull", counter);

    return new PullResponse(counter);
  }

  /**
   * Method description
   *
   *
   * @param git
   * @param tru
   *
   * @return
   */
  private long count(Git git, TrackingRefUpdate tru)
  {
    long counter = 0;

    if (GitUtil.isHead(tru.getLocalName()))
    {
      try
      {
        org.eclipse.jgit.api.LogCommand log = git.log();

        ObjectId oldId = tru.getOldObjectId();

        if (GitUtil.isValidObjectId(oldId))
        {
          log.not(oldId);
        }

        ObjectId newId = tru.getNewObjectId();

        if (GitUtil.isValidObjectId(newId))
        {
          log.add(newId);
        }

        Iterable<RevCommit> commits = log.call();

        if (commits != null)
        {
          counter += Iterables.size(commits);
        }

        logger.trace("counting {} commits for ref update {}", counter, tru);
      }
      catch (Exception ex)
      {
        logger.error("could not count pushed/pulled changesets", ex);
      }
    }
    else
    {
      logger.debug("do not count non branch ref update {}", tru);
    }

    return counter;
  }

  /**
   * Method description
   *
   *
   * @param sourceRepository
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  private PullResponse pullFromScmRepository(Repository sourceRepository)
    throws IOException, RepositoryException
  {
    File sourceDirectory = handler.getDirectory(sourceRepository);

    Preconditions.checkArgument(sourceDirectory.exists(),
      "source repository directory does not exists");

    File targetDirectory = handler.getDirectory(repository);

    Preconditions.checkArgument(sourceDirectory.exists(),
      "target repository directory does not exists");

    logger.debug("pull changes from {} to {}",
      sourceDirectory.getAbsolutePath(), repository.getId());

    PullResponse response = null;

    org.eclipse.jgit.lib.Repository source = null;

    try
    {
      source = Git.open(sourceDirectory).getRepository();
      response = new PullResponse(push(source, getRemoteUrl(targetDirectory)));
    }
    finally
    {
      GitUtil.close(source);
    }

    return response;
  }

  /**
   * Method description
   *
   *
   * @param url
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  private PullResponse pullFromUrl(URL url)
    throws IOException, RepositoryException
  {
    logger.debug("pull changes from {} to {}", url, repository.getId());

    PullResponse response;
    Git git = Git.wrap(open());

    try
    {
      //J-
      FetchResult result = git.fetch()
        .setRefSpecs(new RefSpec(REF_SPEC))
        .setRemote(url.toExternalForm())
        .setTagOpt(TagOpt.FETCH_TAGS)
        .call();
      //J+

      response = convert(git, result);
    }
    catch (GitAPIException ex)
    {
      throw new RepositoryException("error durring pull", ex);
    }

    return response;
  }
}
