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
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.TagOpt;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.PullResponse;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

/**
 * @author Sebastian Sdorra
 */
public class GitPullCommand extends AbstractGitPushOrPullCommand
  implements PullCommand {

  /**
   * Field description
   */
  private static final String REF_SPEC = "refs/heads/*:refs/heads/*";

  /**
   * Field description
   */
  private static final Logger logger =
    LoggerFactory.getLogger(GitPullCommand.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   * @param handler
   * @param context
   */
  @Inject
  public GitPullCommand(GitRepositoryHandler handler, GitContext context) {
    super(handler, context);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   * @param request
   * @return
   * @throws IOException
   */
  @Override
  public PullResponse pull(PullCommandRequest request)
    throws IOException {
    PullResponse response;
    Repository sourceRepository = request.getRemoteRepository();

    if (sourceRepository != null) {
      response = pullFromScmRepository(sourceRepository);
    } else if (request.getRemoteUrl() != null) {
      response = pullFromUrl(request);
    } else {
      throw new IllegalArgumentException("repository or url is required");
    }

    return response;
  }

  private PullResponse convert(Git git, FetchResult fetch) {
    long counter = 0l;

    for (TrackingRefUpdate tru : fetch.getTrackingRefUpdates()) {
      counter += count(git, tru);
    }

    logger.debug("received {} changesets by pull", counter);

    return new PullResponse(counter);
  }

  /**
   * Method description
   *
   * @param git
   * @param tru
   * @return
   */
  private long count(Git git, TrackingRefUpdate tru) {
    long counter = 0;

    if (GitUtil.isHead(tru.getLocalName())) {
      try {
        org.eclipse.jgit.api.LogCommand log = git.log();

        ObjectId oldId = tru.getOldObjectId();

        if (GitUtil.isValidObjectId(oldId)) {
          log.not(oldId);
        }

        ObjectId newId = tru.getNewObjectId();

        if (GitUtil.isValidObjectId(newId)) {
          log.add(newId);
        }

        Iterable<RevCommit> commits = log.call();

        if (commits != null) {
          counter += Iterables.size(commits);
        }

        logger.trace("counting {} commits for ref update {}", counter, tru);
      } catch (Exception ex) {
        logger.error("could not count pushed/pulled changesets", ex);
      }
    } else {
      logger.debug("do not count non branch ref update {}", tru);
    }

    return counter;
  }

  private PullResponse pullFromScmRepository(Repository sourceRepository)
    throws IOException {
    File sourceDirectory = handler.getDirectory(sourceRepository.getId());

    Preconditions.checkArgument(sourceDirectory.exists(),
      "source repository directory does not exists");

    File targetDirectory = handler.getDirectory(repository.getId());

    Preconditions.checkArgument(sourceDirectory.exists(),
      "target repository directory does not exists");

    logger.debug("pull changes from {} to {}",
      sourceDirectory.getAbsolutePath(), repository.getId());

    PullResponse response = null;

    org.eclipse.jgit.lib.Repository source = null;

    try {
      source = Git.open(sourceDirectory).getRepository();
      response = new PullResponse(push(source, getRemoteUrl(targetDirectory)));
    } finally {
      GitUtil.close(source);
    }

    return response;
  }

  private PullResponse pullFromUrl(PullCommandRequest request)
    throws IOException {
    logger.debug("pull changes from {} to {}", request.getRemoteUrl(), repository.getId());

    PullResponse response;
    Git git = Git.wrap(open());

    try {
      //J-
      FetchCommand fetchCommand = git.fetch();

      if (!Strings.isNullOrEmpty(request.getUsername()) && !Strings.isNullOrEmpty(request.getPassword())) {
        fetchCommand.setCredentialsProvider(
          new UsernamePasswordCredentialsProvider(request.getUsername(), request.getPassword())
        );
      }

      FetchResult result = fetchCommand
        .setRefSpecs(new RefSpec(REF_SPEC))
        .setRemote(request.getRemoteUrl().toExternalForm())
        .setTagOpt(TagOpt.FETCH_TAGS)
        .call();
      //J+

      response = convert(git, result);
    } catch (GitAPIException ex) {
      throw new InternalRepositoryException(repository, "error during pull", ex);
    }

    return response;
  }
}
