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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
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
import sonia.scm.ContextEntry;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.Person;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.Tag;
import sonia.scm.repository.api.HookBranchProvider;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookContextFactory;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.repository.api.HookTagProvider;
import sonia.scm.repository.api.ImportFailedException;
import sonia.scm.repository.api.PullResponse;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Sebastian Sdorra
 */
public class GitPullCommand extends AbstractGitPushOrPullCommand
  implements PullCommand {

  private static final String REF_SPEC = "refs/heads/*:refs/heads/*";
  private static final Logger LOG = LoggerFactory.getLogger(GitPullCommand.class);
  private final HookContextFactory hookContextFactory;
  private final ScmEventBus eventBus;

  /**
   * Constructs ...
   *
   * @param handler
   * @param context
   * @param hookContextFactory
   * @param eventBus
   */
  @Inject
  public GitPullCommand(GitRepositoryHandler handler, GitContext context, HookContextFactory hookContextFactory, ScmEventBus eventBus) {
    super(handler, context);
    this.hookContextFactory = hookContextFactory;
    this.eventBus = eventBus;
  }

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
    long counter = 0;

    for (TrackingRefUpdate tru : fetch.getTrackingRefUpdates()) {
      counter += count(git, tru);
    }

    LOG.debug("received {} changesets by pull", counter);

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

        LOG.trace("counting {} commits for ref update {}", counter, tru);
      } catch (Exception ex) {
        LOG.error("could not count pushed/pulled changesets", ex);
      }
    } else {
      LOG.debug("do not count non branch ref update {}", tru);
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

    LOG.debug("pull changes from {} to {}",
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
    LOG.debug("pull changes from {} to {}", request.getRemoteUrl(), repository);

    PullResponse response;
    Git git = Git.wrap(open());
    FetchResult result;
    try {
      //J-
      result = git.fetch()
        .setCredentialsProvider(
          new UsernamePasswordCredentialsProvider(
            Strings.nullToEmpty(request.getUsername()),
            Strings.nullToEmpty(request.getPassword())
          )
        )
        .setRefSpecs(new RefSpec(REF_SPEC))
        .setRemote(request.getRemoteUrl().toExternalForm())
        .setTagOpt(TagOpt.FETCH_TAGS)
        .call();
      //J+

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
    List<String> branches = getBrachesFromFetchResult(result);
    List<Tag> tags = getTagsFromFetchResult(result);
    List<Changeset> changesets = getChangesetsForPulledRepository(git);
    eventBus.post(new PostReceiveRepositoryHookEvent(createPullHookEvent(new PullHookContextProvider(tags, changesets, branches))));
  }

  private List<Changeset> getChangesetsForPulledRepository(Git git) {
    List<Changeset> changesets = new ArrayList<>();
    try {
      for (RevCommit revCommit : git.log().all().call()) {
        changesets.add(
          new Changeset(
            revCommit.getName(),
            GitUtil.getCommitTime(revCommit),
            new Person(revCommit.getAuthorIdent().getName(), revCommit.getAuthorIdent().getEmailAddress()),
            revCommit.getFullMessage()
          )
        );
      }
    } catch (GitAPIException | IOException e) {
      throw new ImportFailedException(ContextEntry.ContextBuilder.entity(repository).build(), "Could not pull changes from remote", e);
    }
    return changesets;
  }

  private List<Tag> getTagsFromFetchResult(FetchResult result) {
    return result.getAdvertisedRefs().stream()
      .filter(r -> r.getName().startsWith("refs/tags"))
      .map(r -> new Tag(r.getName(), r.getObjectId().getName()))
      .collect(Collectors.toList());
  }

  private List<String> getBrachesFromFetchResult(FetchResult result) {
    return result.getAdvertisedRefs().stream()
      .filter(r -> r.getName().startsWith("refs/heads"))
      .map(r -> r.getLeaf().getName())
      .collect(Collectors.toList());
  }

  private RepositoryHookEvent createPullHookEvent(PullHookContextProvider hookEvent) {
    HookContext context = hookContextFactory.createContext(hookEvent, this.context.getRepository());
    return new RepositoryHookEvent(context, this.context.getRepository(), RepositoryHookType.POST_RECEIVE);
  }

  private static class PullHookContextProvider extends HookContextProvider {
    private final List<Tag> newTags;
    private final List<Changeset> newChangesets;
    private final List<String> newBranches;

    private PullHookContextProvider(List<Tag> newTags, List<Changeset> newChangesets, List<String> newBranches) {
      this.newTags = newTags;
      this.newChangesets = newChangesets;
      this.newBranches = newBranches;
    }

    @Override
    public Set<HookFeature> getSupportedFeatures() {
      return ImmutableSet.of(HookFeature.CHANGESET_PROVIDER, HookFeature.BRANCH_PROVIDER, HookFeature.TAG_PROVIDER);
    }

    @Override
    public HookTagProvider getTagProvider() {
      return new HookTagProvider() {
        @Override
        public List<Tag> getCreatedTags() {
          return newTags;
        }

        @Override
        public List<Tag> getDeletedTags() {
          return Collections.emptyList();
        }
      };
    }

    @Override
    public HookBranchProvider getBranchProvider() {
      return new HookBranchProvider() {
        @Override
        public List<String> getCreatedOrModified() {
          return newBranches;
        }

        @Override
        public List<String> getDeletedOrClosed() {
          return Collections.emptyList();
        }
      };
    }

    @Override
    public HookChangesetProvider getChangesetProvider() {
      return r -> new HookChangesetResponse(newChangesets);
    }
  }
}
