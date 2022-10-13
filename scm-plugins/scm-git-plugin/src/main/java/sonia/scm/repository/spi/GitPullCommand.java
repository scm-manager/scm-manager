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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ContextEntry;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.ImportFailedException;
import sonia.scm.repository.api.MirrorCommandResult;
import sonia.scm.repository.api.PullResponse;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Sebastian Sdorra
 */
public class GitPullCommand extends AbstractGitPushOrPullCommand
  implements PullCommand {

  private static final Logger LOG = LoggerFactory.getLogger(GitPullCommand.class);

  private final PostReceiveRepositoryHookEventFactory postReceiveRepositoryHookEventFactory;
  private final LfsLoader lfsLoader;

  @Inject
  public GitPullCommand(GitRepositoryHandler handler,
                        GitContext context,
                        PostReceiveRepositoryHookEventFactory postReceiveRepositoryHookEventFactory, LfsLoader lfsLoader) {
    super(handler, context);
    this.postReceiveRepositoryHookEventFactory = postReceiveRepositoryHookEventFactory;
    this.lfsLoader = lfsLoader;
  }

  @Override
  public PullResponse pull(PullCommandRequest request)
    throws IOException {
    PullResponse response;
    Repository sourceRepository = request.getRemoteRepository();

    if (sourceRepository != null) {
      response = pullFromScmRepository(sourceRepository, request.getUsername(), request.getPassword());
    } else if (request.getRemoteUrl() != null) {
      response = pullFromUrl(request);
    } else {
      throw new IllegalArgumentException("repository or url is required");
    }

    return response;
  }

  private PullResponse convert(Git git, FetchResult fetch) {
    long counter = 0;

    for (TrackingRefUpdate tru : fetch.getTrackingRefUpdates()) {
      counter += count(git, tru);
    }

    LOG.debug("received {} changesets by pull", counter);

    return new PullResponse(counter);
  }

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

        LOG.trace("counting {} commits for ref update {}", counter, tru);
      } catch (Exception ex) {
        LOG.error("could not count pushed/pulled changesets", ex);
      }
    } else {
      LOG.debug("do not count non branch ref update {}", tru);
    }

    return counter;
  }

  private PullResponse pullFromScmRepository(Repository sourceRepository, String username, String password)
    throws IOException {
    File sourceDirectory = handler.getDirectory(sourceRepository.getId());

    Preconditions.checkArgument(sourceDirectory.exists(),
      "source repository directory does not exists");

    File targetDirectory = handler.getDirectory(repository.getId());

    Preconditions.checkArgument(sourceDirectory.exists(),
      "target repository directory does not exists");

    LOG.debug("pull changes from {} to {}",
      sourceDirectory.getAbsolutePath(), repository.getId());

    PullResponse response;

    org.eclipse.jgit.lib.Repository source = null;

    try (Git git = Git.open(sourceDirectory)) {
      source = git.getRepository();
      response = new PullResponse(push(source, getRemoteUrl(targetDirectory), username, password));
    } finally {
      GitUtil.close(source);
    }

    return response;
  }

  private PullResponse pullFromUrl(PullCommandRequest request)
    throws IOException {
    LOG.debug("pull changes from {} to {}", request.getRemoteUrl(), repository);

    PullResponse response;
    Git git = Git.wrap(open());
    FetchResult result;
    try {
      //J-
      result = GitUtil.createFetchCommandWithBranchAndTagUpdate(git)
        .setCredentialsProvider(
          new UsernamePasswordCredentialsProvider(
              Strings.nullToEmpty(request.getUsername()),
            Strings.nullToEmpty(request.getPassword())
          )
        )
        .setRemote(request.getRemoteUrl().toExternalForm())
        .call();
      //J+

      if (request.isFetchLfs()) {
        open().getRefDatabase().getRefs().forEach(
          ref -> lfsLoader.inspectTree(
            ref.getObjectId(),
            request,
            git.getRepository(),
            new ArrayList<>(),
            new MirrorCommandResult.LfsUpdateResult(),
            repository,
            request.getRemoteUrl().toString())
        );
      }
      response = convert(git, result);
    } catch
    (GitAPIException ex) {
      throw new ImportFailedException(
        ContextEntry.ContextBuilder.entity(repository).build(),
        "Repository import failed. The credentials are wrong or missing.",
        ex
      );
    }

    firePostReceiveRepositoryHookEvent(git, result);

    return response;
  }

  private void firePostReceiveRepositoryHookEvent(Git git, FetchResult result) {
    postReceiveRepositoryHookEventFactory.fireForFetch(git, result);
  }
}
