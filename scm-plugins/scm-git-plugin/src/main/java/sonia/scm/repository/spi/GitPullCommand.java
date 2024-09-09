/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.repository.spi;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ContextEntry;
import sonia.scm.api.v2.resources.GitRepositoryConfigStoreProvider;
import sonia.scm.repository.GitHeadModifier;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.ImportFailedException;
import sonia.scm.repository.api.MirrorCommandResult;
import sonia.scm.repository.api.PullResponse;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


public class GitPullCommand extends AbstractGitPushOrPullCommand
  implements PullCommand {

  private static final Logger LOG = LoggerFactory.getLogger(GitPullCommand.class);

  private final PostReceiveRepositoryHookEventFactory postReceiveRepositoryHookEventFactory;
  private final LfsLoader lfsLoader;
  private final PullHttpConnectionProvider pullHttpConnectionProvider;
  private final GitRepositoryConfigStoreProvider storeProvider;
  private final GitHeadModifier gitHeadModifier;

  @Inject
  public GitPullCommand(GitRepositoryHandler handler,
                        @Assisted GitContext context,
                        @Assisted PostReceiveRepositoryHookEventFactory postReceiveRepositoryHookEventFactory,
                        LfsLoader lfsLoader,
                        PullHttpConnectionProvider pullHttpConnectionProvider,
                        GitRepositoryConfigStoreProvider storeProvider, GitHeadModifier gitHeadModifier) {
    super(handler, context);
    this.postReceiveRepositoryHookEventFactory = postReceiveRepositoryHookEventFactory;
    this.lfsLoader = lfsLoader;
    this.pullHttpConnectionProvider = pullHttpConnectionProvider;
    this.storeProvider = storeProvider;
    this.gitHeadModifier = gitHeadModifier;
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

  private PullResponse convert(Git git, FetchResult fetch, CountingLfsLoaderLogger lfsLoaderLogger) {
    long counter = 0;

    for (TrackingRefUpdate tru : fetch.getTrackingRefUpdates()) {
      counter += count(git, tru);
    }

    LOG.debug("received {} changesets by pull", counter);

    return new PullResponse(counter, new PullResponse.LfsCount(lfsLoaderLogger.getSuccessCount(), lfsLoaderLogger.getFailureCount()));
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
      response = new PullResponse(push(source, getRemoteUrl(targetDirectory), username, password, false));
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

      CountingLfsLoaderLogger lfsLoaderLogger = new CountingLfsLoaderLogger();
      if (request.isFetchLfs()) {
        fetchLfs(request, git, lfsLoaderLogger);
      }

      configureDefaultBranch(result);

      response = convert(git, result, lfsLoaderLogger);
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

  private void configureDefaultBranch(FetchResult result) {
    Ref head = result.getAdvertisedRef("HEAD").getLeaf();
    if (head.getName().startsWith("refs/heads/")) {
      String newDefaultBranch = head.getName().substring("refs/heads/".length());
      gitHeadModifier.ensure(repository, newDefaultBranch);
      storeProvider.setDefaultBranch(repository, newDefaultBranch);
    }
  }

  private void fetchLfs(PullCommandRequest request, Git git, LfsLoader.LfsLoaderLogger lfsLoaderLogger) throws IOException {
    Set<ObjectId> alreadyVisited = new HashSet<>(1000);
    open().getRefDatabase().getRefs().forEach(
      ref -> lfsLoader.inspectTree(
        ref.getObjectId(),
        git.getRepository(),
        lfsLoaderLogger,
        new MirrorCommandResult.LfsUpdateResult(),
        repository,
        pullHttpConnectionProvider.createHttpConnectionFactory(request),
        request.getRemoteUrl().toString(),
        alreadyVisited
      )
    );
  }

  private void firePostReceiveRepositoryHookEvent(Git git, FetchResult result) {
    postReceiveRepositoryHookEventFactory.fireForFetch(git, result);
  }

  private static class CountingLfsLoaderLogger implements LfsLoader.LfsLoaderLogger {

    private int successCount = 0;
    private int failureCount = 0;

    @Override
    public void failed(Exception e) {
      ++failureCount;
    }

    @Override
    public void loading(String name) {
      ++successCount;
    }

    public int getSuccessCount() {
      return successCount;
    }

    public int getFailureCount() {
      return failureCount;
    }
  }

  public interface Factory {
    PullCommand create(GitContext context, PostReceiveRepositoryHookEventFactory postReceiveRepositoryHookEventFactory);
  }

}
