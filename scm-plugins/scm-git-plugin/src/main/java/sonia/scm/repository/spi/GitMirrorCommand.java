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

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefDatabase;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.transport.TransportHttp;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.api.v2.resources.GitRepositoryConfigStoreProvider;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.GitChangesetConverter;
import sonia.scm.repository.GitChangesetConverterFactory;
import sonia.scm.repository.GitHeadModifier;
import sonia.scm.repository.GitRepositoryConfig;
import sonia.scm.repository.GitWorkingCopyFactory;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Tag;
import sonia.scm.repository.api.MirrorCommandResult;
import sonia.scm.repository.api.MirrorCommandResult.ResultType;
import sonia.scm.repository.api.MirrorFilter;
import sonia.scm.repository.api.MirrorFilter.Result;
import sonia.scm.repository.api.UsernamePasswordCredential;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.web.lfs.LfsBlobStoreFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.eclipse.jgit.lib.RefUpdate.Result.NEW;
import static sonia.scm.repository.api.MirrorCommandResult.ResultType.FAILED;
import static sonia.scm.repository.api.MirrorCommandResult.ResultType.OK;
import static sonia.scm.repository.api.MirrorCommandResult.ResultType.REJECTED_UPDATES;

/**
 * Implementation of the mirror command for git.
 *
 * The general workflow is the same for the first call and all subsequent updates and looks like this:
 *
 * <ol>
 *   <li>Create a local working copy for the repository.</li>
 *   <li>Fetch updates from the source and override all local refs in the working copy
 *     (like <code>git fetch "refs/heads/*:refs/heads/*" "refs/tags/*:refs/tags/*"</code>).</li>
 *   <li>Iterate the updates and decide, whether to keep each single update or not. If an update is rejected,
 *     the reference is set to its old oid (or deleted, if this is a new reference).</li>
 *   <li>Push the changed references from the working copy to the repository.</li>
 *   <li>Release the working copy.</li>
 * </ol>
 */
public class GitMirrorCommand extends AbstractGitCommand implements MirrorCommand {

  private static final Logger LOG = LoggerFactory.getLogger(GitMirrorCommand.class);

  private final MirrorHttpConnectionProvider mirrorHttpConnectionProvider;
  private final GitChangesetConverterFactory converterFactory;
  private final GitTagConverter gitTagConverter;
  private final GitWorkingCopyFactory workingCopyFactory;
  private final GitHeadModifier gitHeadModifier;
  private final GitRepositoryConfigStoreProvider storeProvider;
  private final LfsLoader lfsLoader;

  @Inject
  GitMirrorCommand(GitContext context,
                   MirrorHttpConnectionProvider mirrorHttpConnectionProvider,
                   GitChangesetConverterFactory converterFactory,
                   GitTagConverter gitTagConverter,
                   GitWorkingCopyFactory workingCopyFactory,
                   GitHeadModifier gitHeadModifier,
                   GitRepositoryConfigStoreProvider storeProvider,
                   LfsLoader lfsLoader) {
    super(context);
    this.mirrorHttpConnectionProvider = mirrorHttpConnectionProvider;
    this.converterFactory = converterFactory;
    this.gitTagConverter = gitTagConverter;
    this.workingCopyFactory = workingCopyFactory;
    this.gitHeadModifier = gitHeadModifier;
    this.storeProvider = storeProvider;
    this.lfsLoader = lfsLoader;
  }

  @Override
  public MirrorCommandResult mirror(MirrorCommandRequest mirrorCommandRequest) {
    return update(mirrorCommandRequest);
  }

  @Override
  public MirrorCommandResult update(MirrorCommandRequest mirrorCommandRequest) {
    // we have to select an existing branch here (and we cannot depend on a correct "default branch" in the
    // configuration), because otherwise the clone will fail with an unresolvable branch.
    String headBranch = resolveHeadBranch();
    return inClone(git -> new Worker(context, mirrorCommandRequest, this.repository, git), workingCopyFactory, headBranch);
  }

  private String resolveHeadBranch() {
    try {
      Repository repository = context.open();
      Ref headRef = repository.findRef(Constants.HEAD);
      if (headRef == null) {
        throw new InternalRepositoryException(context.getRepository(), "Cannot handle missing HEAD in repository");
      }
      return headRef.getTarget().getName().substring("refs/heads/".length());
    } catch (IOException e) {
      throw new InternalRepositoryException(context.getRepository(), "could not resolve HEAD", e);
    }
  }

  private class Worker extends GitCloneWorker<MirrorCommandResult> {

    private final MirrorCommandRequest mirrorCommandRequest;
    private final List<String> mirrorLog = new ArrayList<>();
    private final Stopwatch stopwatch;

    private final DefaultBranchSelector defaultBranchSelector;

    private final Git git;

    private final Collection<String> deletedRefs = new ArrayList<>();
    private final MirrorCommandResult.LfsUpdateResult lfsUpdateResult = new MirrorCommandResult.LfsUpdateResult();

    private FetchResult fetchResult;
    private GitFilterContext filterContext;
    private MirrorFilter.Filter filter;

    private ResultType result = OK;

    private Worker(GitContext context, MirrorCommandRequest mirrorCommandRequest, sonia.scm.repository.Repository repository, Git git) {
      super(git, context, repository);
      this.mirrorCommandRequest = mirrorCommandRequest;
      this.git = git;
      stopwatch = Stopwatch.createStarted();
      defaultBranchSelector = new DefaultBranchSelector(git);
    }

    MirrorCommandResult run() {
      try {
        return doUpdate();
      } catch (GitAPIException e) {
        result = FAILED;
        LOG.info("got exception while trying to synchronize mirror for repository {}", context.getRepository(), e);
        mirrorLog.add("failed to synchronize: " + e.getMessage());
        return new MirrorCommandResult(FAILED, mirrorLog, stopwatch.stop().elapsed(), lfsUpdateResult);
      }
    }

    private MirrorCommandResult doUpdate() throws GitAPIException {
      copyRemoteRefsToMain();
      fetchResult = createFetchCommand().call();
      filterContext = new GitFilterContext();
      filter = mirrorCommandRequest.getFilter().getFilter(filterContext);

      if (fetchResult.getTrackingRefUpdates().isEmpty()) {
        LOG.trace("No updates found for mirror repository {}", repository);
        mirrorLog.add("No updates found");
        return new MirrorCommandResult(result, mirrorLog, stopwatch.stop().elapsed(), lfsUpdateResult);
      } else {
        handleBranches();
        handleTags();
      }

      if (!defaultBranchSelector.isChanged()) {
        mirrorLog.add("No effective changes detected");
        return new MirrorCommandResult(result, mirrorLog, stopwatch.stop().elapsed(), lfsUpdateResult);
      }

      defaultBranchSelector.newDefaultBranch().ifPresent(this::setNewDefaultBranch);

      String[] pushRefSpecs = generatePushRefSpecs().toArray(new String[0]);
      push(pushRefSpecs);
      ResultType finalResult = lfsUpdateResult.hasFailures()? FAILED: result;
      return new MirrorCommandResult(finalResult, mirrorLog, stopwatch.stop().elapsed(), lfsUpdateResult);
    }

    private void setNewDefaultBranch(String newDefaultBranch) {
      mirrorLog.add("Old default branch deleted. Setting default branch to '" + newDefaultBranch + "'.");

      try {
        String oldBranch = git.getRepository().getBranch();
        RefUpdate refUpdate = git.getRepository().getRefDatabase().newUpdate(Constants.HEAD, true);
        refUpdate.setForceUpdate(true);
        RefUpdate.Result result = refUpdate.link(Constants.R_HEADS + newDefaultBranch);
        if (result != RefUpdate.Result.FORCED) {
          throw new InternalRepositoryException(getRepository(), "Could not set HEAD to new default branch");
        }
        git.branchDelete().setBranchNames(oldBranch).setForce(true).call();
      } catch (GitAPIException | IOException e) {
        throw new InternalRepositoryException(getRepository(), "Error while switching branch to change default branch", e);
      }

      gitHeadModifier.ensure(repository, newDefaultBranch);
      ConfigurationStore<GitRepositoryConfig> configStore = storeProvider.get(repository);
      GitRepositoryConfig gitRepositoryConfig = configStore.get();
      gitRepositoryConfig.setDefaultBranch(newDefaultBranch);
      configStore.set(gitRepositoryConfig);
    }

    private Collection<String> generatePushRefSpecs() {
      Collection<String> refSpecs = new ArrayList<>();
      refSpecs.add("refs/heads/*:refs/heads/*");
      refSpecs.add("refs/tags/*:refs/tags/*");
      deletedRefs.forEach(deletedRef -> refSpecs.add(":" + deletedRef));
      return refSpecs;
    }

    private void copyRemoteRefsToMain() {
      LOG.trace("Copy remote refs to main");
      try {
        RefDatabase refDatabase = git.getRepository().getRefDatabase();
        refDatabase.getRefs()
          .stream()
          .filter(ref -> ref.getName().startsWith("refs/remotes/origin/"))
          .forEach(
          ref -> {
            try {
              LOG.trace("Copying reference {}", ref);
              String baseName = ref.getName().substring("refs/remotes/origin/".length());
              RefUpdate refUpdate = refDatabase.newUpdate("refs/heads/" + baseName, true);
              refUpdate.setNewObjectId(ref.getObjectId());
              refUpdate.forceUpdate();
            } catch (IOException e) {
              throw new InternalRepositoryException(context.getRepository(), "Failed to copy origin remote refs " + ref.getName(), e);
            }
          }
        );
      } catch (IOException e) {
        throw new InternalRepositoryException(context.getRepository(), "Failed to copy remote refs", e);
      }
    }

    private void handleBranches() {
      LoggerWithHeader logger = new LoggerWithHeader("Branches:");
      doForEachRefStartingWith("refs/heads", ref -> handleBranch(logger, ref));
    }

    private void handleBranch(LoggerWithHeader logger, TrackingRefUpdate ref) {
      MirrorReferenceUpdateHandler refHandler = new MirrorReferenceUpdateHandler(logger, ref, RefType.BRANCH);
      refHandler.handleRef(ref1 -> refHandler.testFilterForBranch());
    }

    private void handleTags() {
      LoggerWithHeader logger = new LoggerWithHeader("Tags:");
      doForEachRefStartingWith("refs/tags", ref -> handleTag(logger, ref));
    }

    private void handleTag(LoggerWithHeader logger, TrackingRefUpdate ref) {
      MirrorReferenceUpdateHandler refHandler = new MirrorReferenceUpdateHandler(logger, ref, RefType.TAG);
      refHandler.handleRef(ref1 -> refHandler.testFilterForTag());
    }

    private class MirrorReferenceUpdateHandler {
      private final LoggerWithHeader logger;
      private final TrackingRefUpdate ref;
      private final RefType refType;

      public MirrorReferenceUpdateHandler(LoggerWithHeader logger, TrackingRefUpdate ref, RefType refType) {
        this.logger = logger;
        this.ref = ref;
        this.refType = refType;
      }

      private void handleRef(Function<TrackingRefUpdate, Result> filter) {
        LOG.trace("Handling {}", ref.getLocalName());
        Result filterResult = filter.apply(ref);
        try {
          String referenceName = ref.getLocalName().substring("refs/".length() + refType.refPath.length());
          if (filterResult.isAccepted()) {
            LOG.trace("Accepted ref {}", ref.getLocalName());
            handleAcceptedReference(referenceName);
          } else {
            LOG.trace("Rejected ref {}", ref.getLocalName());
            handleRejectedRef(referenceName, filterResult);
          }
        } catch (Exception e) {
          handleReferenceUpdateException(e);
        }
      }

      private Result testFilterForBranch() {
        try {
          return filter.acceptBranch(filterContext.getBranchUpdate(ref.getLocalName()));
        } catch (Exception e) {
          return handleExceptionFromFilter(e);
        }
      }

      private void handleReferenceUpdateException(Exception e) {
        LOG.warn("got exception processing ref {} in repository {}", ref.getLocalName(), GitMirrorCommand.this.repository, e);
        mirrorLog.add(format("got error processing reference %s: %s", ref.getLocalName(), e.getMessage()));
        mirrorLog.add("mirror may be damaged");
      }

      private void handleRejectedRef(String referenceName, Result filterResult) throws IOException {
        result = REJECTED_UPDATES;
        LOG.trace("{} ref rejected in {}: {}", refType.typeForLog, GitMirrorCommand.this.repository, ref.getLocalName());
        if (ref.getResult() == NEW) {
          deleteReference(ref.getLocalName());
        } else {
          updateReference(ref.getLocalName(), ref.getOldObjectId());
        }
        logger.logChange(ref, referenceName, filterResult.getRejectReason().orElse("rejected due to filter"));
      }

      private void handleAcceptedReference(String referenceName) throws IOException {
        String targetRef = "refs/" + refType.refPath + referenceName;
        if (isDeletedReference(ref)) {
          LOG.trace("deleting {} ref in {}: {}", refType.typeForLog, GitMirrorCommand.this.repository, targetRef);
          defaultBranchSelector.deleted(refType, referenceName);
          logger.logChange(ref, referenceName, "deleted");
          deleteReference(targetRef);
          deletedRefs.add(targetRef);
        } else {
          LOG.trace("updating {} ref in {}: {}", refType.typeForLog, GitMirrorCommand.this.repository, targetRef);
          defaultBranchSelector.accepted(refType, referenceName);
          logger.logChange(ref, referenceName, getUpdateType(ref));

          if (!mirrorCommandRequest.isIgnoreLfs()) {
            lfsLoader.inspectTree(ref.getNewObjectId(), mirrorCommandRequest, git.getRepository(), mirrorLog, lfsUpdateResult, repository);
          }
        }
      }

      private Result testFilterForTag() {
        try {
          return filter.acceptTag(filterContext.getTagUpdate(ref.getLocalName()));
        } catch (Exception e) {
          return handleExceptionFromFilter(e);
        }
      }

      private Result handleExceptionFromFilter(Exception e) {
        LOG.warn("got exception from filter for ref {} in repository {}", ref.getLocalName(), GitMirrorCommand.this.repository, e);
        mirrorLog.add("! got error checking filter for update: " + e.getMessage());
        return Result.reject("exception in filter");
      }

      private void deleteReference(String targetRef) throws IOException {
        RefUpdate deleteUpdate = git.getRepository().getRefDatabase().newUpdate(targetRef, true);
        deleteUpdate.setForceUpdate(true);
        deleteUpdate.delete();
      }

      private boolean isDeletedReference(TrackingRefUpdate ref) {
        return ref.asReceiveCommand().getType() == ReceiveCommand.Type.DELETE;
      }

      private void updateReference(String reference, ObjectId objectId) throws IOException {
        LOG.trace("updating ref in {}: {} -> {}", GitMirrorCommand.this.repository, reference, objectId);
        RefUpdate refUpdate = git.getRepository().getRefDatabase().newUpdate(reference, true);
        refUpdate.setNewObjectId(objectId);
        refUpdate.forceUpdate();
      }

      private String getUpdateType(TrackingRefUpdate trackingRefUpdate) {
        return trackingRefUpdate.getResult().name().toLowerCase(Locale.ENGLISH);
      }
    }

    private class LoggerWithHeader {
      private final String header;
      private boolean headerWritten = false;

      private LoggerWithHeader(String header) {
        this.header = header;
      }

      void logChange(TrackingRefUpdate ref, String branchName, String type) {
        logLine(
          format("- %s..%s %s (%s)",
            ref.getOldObjectId().abbreviate(9).name(),
            ref.getNewObjectId().abbreviate(9).name(),
            branchName,
            type
          ));
      }

      void logLine(String line) {
        if (!headerWritten) {
          headerWritten = true;
          mirrorLog.add(header);
        }
        mirrorLog.add(line);
      }
    }

    private void doForEachRefStartingWith(String prefix, RefUpdateConsumer refUpdateConsumer) {
      fetchResult.getTrackingRefUpdates()
        .stream()
        .filter(ref -> ref.getLocalName().startsWith(prefix))
        .forEach(ref -> {
          try {
            refUpdateConsumer.accept(ref);
          } catch (IOException e) {
            throw new InternalRepositoryException(GitMirrorCommand.this.repository, "error updating mirror references", e);
          }
        });
    }

    private FetchCommand createFetchCommand() {
      FetchCommand fetchCommand = Git.wrap(git.getRepository()).fetch()
        .setRefSpecs("refs/heads/*:refs/heads/*", "refs/tags/*:refs/tags/*")
        .setForceUpdate(true)
        .setRemoveDeletedRefs(true)
        .setRemote(mirrorCommandRequest.getSourceUrl())
        .setTransportConfigCallback(transport -> {
          if (transport instanceof TransportHttp) {
            TransportHttp transportHttp = (TransportHttp) transport;
            transportHttp.setHttpConnectionFactory(mirrorHttpConnectionProvider.createHttpConnectionFactory(mirrorCommandRequest, mirrorLog));
          }
        });

      mirrorCommandRequest.getCredential(UsernamePasswordCredential.class)
        .ifPresent(c -> fetchCommand
          .setCredentialsProvider(
            new UsernamePasswordCredentialsProvider(
              Strings.nullToEmpty(c.username()),
              Strings.nullToEmpty(new String(c.password()))
            ))
        );

      return fetchCommand;
    }

    private class GitFilterContext implements MirrorFilter.FilterContext {

      private final Map<String, MirrorFilter.BranchUpdate> branchUpdates;
      private final Map<String, MirrorFilter.TagUpdate> tagUpdates;

      public GitFilterContext() {
        Map<String, MirrorFilter.BranchUpdate> extractedBranchUpdates = new HashMap<>();
        Map<String, MirrorFilter.TagUpdate> extractedTagUpdates = new HashMap<>();

        fetchResult.getTrackingRefUpdates().forEach(refUpdate -> {
          if (refUpdate.getLocalName().startsWith("refs/heads")) {
            extractedBranchUpdates.put(refUpdate.getLocalName(), new GitBranchUpdate(refUpdate));
          }
          if (refUpdate.getLocalName().startsWith("refs/tags")) {
            extractedTagUpdates.put(refUpdate.getLocalName(), new GitTagUpdate(refUpdate));
          }
        });

        this.branchUpdates = unmodifiableMap(extractedBranchUpdates);
        this.tagUpdates = unmodifiableMap(extractedTagUpdates);
      }

      @Override
      public Collection<MirrorFilter.BranchUpdate> getBranchUpdates() {
        return branchUpdates.values();
      }

      @Override
      public Collection<MirrorFilter.TagUpdate> getTagUpdates() {
        return tagUpdates.values();
      }

      MirrorFilter.BranchUpdate getBranchUpdate(String ref) {
        return branchUpdates.get(ref);
      }

      MirrorFilter.TagUpdate getTagUpdate(String ref) {
        return tagUpdates.get(ref);
      }
    }

    private class GitBranchUpdate implements MirrorFilter.BranchUpdate {

      private final TrackingRefUpdate refUpdate;

      private final String branchName;

      private Changeset changeset;

      public GitBranchUpdate(TrackingRefUpdate refUpdate) {
        this.refUpdate = refUpdate;
        this.branchName = refUpdate.getLocalName().substring("refs/heads/".length());
      }

      @Override
      public String getBranchName() {
        return branchName;
      }

      @Override
      public Optional<Changeset> getChangeset() {
        if (isOfTypeOrEmpty(getUpdateType(), MirrorFilter.UpdateType.DELETE)) {
          return empty();
        }
        if (changeset == null) {
          changeset = computeChangeset();
        }
        return of(changeset);
      }

      @Override
      public Optional<String> getNewRevision() {
        if (isOfTypeOrEmpty(getUpdateType(), MirrorFilter.UpdateType.DELETE)) {
          return empty();
        }
        return of(refUpdate.getNewObjectId().name());
      }

      @Override
      public Optional<String> getOldRevision() {
        if (isOfTypeOrEmpty(getUpdateType(), MirrorFilter.UpdateType.CREATE)) {
          return empty();
        }
        return of(refUpdate.getOldObjectId().name());
      }

      @Override
      public Optional<MirrorFilter.UpdateType> getUpdateType() {
        return getUpdateTypeFor(refUpdate.asReceiveCommand());
      }

      @Override
      public boolean isForcedUpdate() {
        return refUpdate.getResult() == RefUpdate.Result.FORCED;
      }

      private Changeset computeChangeset() {
        try (RevWalk revWalk = new RevWalk(git.getRepository()); GitChangesetConverter gitChangesetConverter = converter(revWalk)) {
          RevCommit revCommit = revWalk.parseCommit(refUpdate.getNewObjectId());
          return gitChangesetConverter.createChangeset(revCommit, refUpdate.getLocalName());
        } catch (Exception e) {
          throw new InternalRepositoryException(context.getRepository(), "got exception while validating branch", e);
        }
    }

    private GitChangesetConverter converter(RevWalk revWalk) {
      return converterFactory.builder(git.getRepository())
        .withRevWalk(revWalk)
        .withAdditionalPublicKeys(mirrorCommandRequest.getPublicKeys())
        .create();
      }
    }

    private class GitTagUpdate implements MirrorFilter.TagUpdate {

      private final TrackingRefUpdate refUpdate;

      private final String tagName;

      private Tag tag;

      public GitTagUpdate(TrackingRefUpdate refUpdate) {
        this.refUpdate = refUpdate;
        this.tagName = refUpdate.getLocalName().substring("refs/tags/".length());
      }

      @Override
      public String getTagName() {
        return tagName;
      }

      @Override
      public Optional<Tag> getTag() {
        if (isOfTypeOrEmpty(getUpdateType(), MirrorFilter.UpdateType.DELETE)) {
          return empty();
        }
        if (tag == null) {
          tag = computeTag();
        }
        return of(tag);
      }

      @Override
      public Optional<String> getNewRevision() {
        return getTag().map(Tag::getRevision);
      }

      @Override
      public Optional<String> getOldRevision() {
        if (isOfTypeOrEmpty(getUpdateType(), MirrorFilter.UpdateType.CREATE)) {
          return empty();
        }
        return of(refUpdate.getOldObjectId().name());
      }

      @Override
      public Optional<MirrorFilter.UpdateType> getUpdateType() {
        return getUpdateTypeFor(refUpdate.asReceiveCommand());
      }

      private Tag computeTag() {
        try (RevWalk revWalk = new RevWalk(git.getRepository())) {
          RevObject revObject = revWalk.parseAny(refUpdate.getNewObjectId());
          if (revObject.getType() == Constants.OBJ_TAG) {
            RevTag revTag = revWalk.parseTag(revObject.getId());
            return gitTagConverter.buildTag(revTag, revWalk);
          } else if (revObject.getType() == Constants.OBJ_COMMIT) {
            Ref ref = git.getRepository().getRefDatabase().findRef(refUpdate.getLocalName());
            Tag t = gitTagConverter.buildTag(git.getRepository(), revWalk, ref);
            return new Tag(tagName, t.getRevision(), t.getDate().orElse(null), t.getDeletable());
          } else {
            throw new InternalRepositoryException(context.getRepository(), "invalid object type for tag");
          }
        } catch (Exception e) {
          throw new InternalRepositoryException(context.getRepository(), "got exception while validating tag", e);
        }
      }
    }

    private boolean isOfTypeOrEmpty(Optional<MirrorFilter.UpdateType> updateType, MirrorFilter.UpdateType type) {
      return !updateType.isPresent() || updateType.get() == type;
    }

    private Optional<MirrorFilter.UpdateType> getUpdateTypeFor(ReceiveCommand receiveCommand) {
      switch (receiveCommand.getType()) {
        case UPDATE:
        case UPDATE_NONFASTFORWARD:
          return of(MirrorFilter.UpdateType.UPDATE);
        case CREATE:
          return of(MirrorFilter.UpdateType.CREATE);
        case DELETE:
          return of(MirrorFilter.UpdateType.DELETE);
        default:
          return empty();
      }
    }
  }

  private interface RefUpdateConsumer {
    void accept(TrackingRefUpdate refUpdate) throws IOException;
  }

  static class DefaultBranchSelector {
    private final String initialDefaultBranch;
    private final Set<String> initialBranches;
    private final Set<String> remainingBranches;
    private final Set<String> newBranches = new HashSet<>();

    private boolean changed = false;

    DefaultBranchSelector(String initialDefaultBranch, Collection<String> initialBranches) {
      this.initialDefaultBranch = initialBranches.isEmpty() ? null : initialDefaultBranch;
      this.initialBranches = new HashSet<>(initialBranches);
      this.remainingBranches = new HashSet<>(initialBranches);
    }

    public DefaultBranchSelector(Git git) {
      this(getInitialDefaultBranch(git), getBranches(git));
    }

    private static Collection<String> getBranches(Git git) {
      Set<String> allBranches = new HashSet<>();
      try {
        git.getRepository()
          .getRefDatabase()
          .getRefsByPrefix("refs/heads")
          .stream()
          .map(Ref::getName)
          .map(ref -> ref.substring("refs/heads/".length()))
          .forEach(allBranches::add);
        git.getRepository()
          .getRefDatabase()
          .getRefsByPrefix("refs/remotes/origin")
          .stream()
          .map(Ref::getName)
          .map(ref -> ref.substring("refs/remotes/origin/".length()))
          .forEach(allBranches::add);
      } catch (IOException e) {
        throw new InternalRepositoryException(emptyList(), "Could not read existing branches for working copy of mirror", e);
      }
      return allBranches;
    }

    private static String getInitialDefaultBranch(Git git) {
      try {
        return git.getRepository().getBranch();
      } catch (IOException e) {
        throw new InternalRepositoryException(emptyList(), "Could not read current branch for working copy of mirror", e);
      }
    }

    public void accepted(RefType refType, String ref) {
      changed = true;
      if (refType == RefType.BRANCH) {
        newBranches.add(ref);
      }
    }

    public void deleted(RefType refType, String ref) {
      changed = true;
      if (refType == RefType.BRANCH) {
        remainingBranches.remove(ref);
      }
    }

    public boolean isChanged() {
      return changed;
    }

    public Optional<String> newDefaultBranch() {
      if (initialDefaultBranch == null && newBranches.contains("master") || remainingBranches.contains(initialDefaultBranch)) {
        return empty();
      } else if (!newBranches.isEmpty() && initialBranches.isEmpty()) {
        return of(newBranches.iterator().next());
      } else if (initialDefaultBranch == null && newBranches.isEmpty()) {
        LOG.info("Could not mirror repository with tags only.");
        throw new IllegalStateException("No branch could be mirrored. Initial mirror without accepted branch is not supported. Make sure that at least one branch can be mirrored.");
      } else if (remainingBranches.isEmpty()) {
        LOG.info("Could not compute new default branch.");
        throw new IllegalStateException("Deleting all existing branches is not supported. Please restore branch '" + initialDefaultBranch + "' or recreate the mirror.");
      } else {
        return of(remainingBranches.iterator().next());
      }
    }
  }

  enum RefType {
    BRANCH("heads/", "branch"), TAG("tags/", "tag");

    private final String refPath;
    private final String typeForLog;

    RefType(String refPath, String typeForLog) {
      this.refPath = refPath;
      this.typeForLog = typeForLog;
    }
  }
}
