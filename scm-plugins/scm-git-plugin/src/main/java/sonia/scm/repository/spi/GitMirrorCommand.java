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
import sonia.scm.repository.Changeset;
import sonia.scm.repository.GitChangesetConverter;
import sonia.scm.repository.GitChangesetConverterFactory;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Tag;
import sonia.scm.repository.api.MirrorCommandResult;
import sonia.scm.repository.api.MirrorCommandResult.ResultType;
import sonia.scm.repository.api.MirrorFilter;
import sonia.scm.repository.api.MirrorFilter.Result;
import sonia.scm.repository.api.Pkcs12ClientCertificateCredential;
import sonia.scm.repository.api.UsernamePasswordCredential;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.eclipse.jgit.lib.RefUpdate.Result.NEW;
import static sonia.scm.repository.api.MirrorCommandResult.ResultType.FAILED;
import static sonia.scm.repository.api.MirrorCommandResult.ResultType.OK;
import static sonia.scm.repository.api.MirrorCommandResult.ResultType.REJECTED_UPDATES;

/**
 * Implementation of the mirror command for git. This implementation makes use of a special
 * "ref" called <code>mirror</code>. A synchronization works in principal in the following way:
 * <ol>
 *   <li>The mirror reference is updated. This is done by calling the jgit equivalent of
 *     <pre>git fetch -pf <source url> "refs/heads/*:refs/mirror/heads/*" "refs/tags/*:refs/mirror/tags/*"</pre>
 *   </li>
 *   <li>These updates are then presented to the filter. Here single updates can be rejected.
 *     Such rejected updates have to be reverted in the mirror, too.
 *   </li>
 *   <li>Accepted ref updates are copied to the "normal" refs.</li>
 * </ol>
 */
public class GitMirrorCommand extends AbstractGitCommand implements MirrorCommand {

  public static final String MIRROR_REF_PREFIX = "refs/mirror/";

  private static final Logger LOG = LoggerFactory.getLogger(GitMirrorCommand.class);

  private final PostReceiveRepositoryHookEventFactory postReceiveRepositoryHookEventFactory;
  private final MirrorHttpConnectionProvider mirrorHttpConnectionProvider;
  private final GitChangesetConverterFactory converterFactory;
  private final GitTagConverter gitTagConverter;

  @Inject
  GitMirrorCommand(GitContext context, PostReceiveRepositoryHookEventFactory postReceiveRepositoryHookEventFactory, MirrorHttpConnectionProvider mirrorHttpConnectionProvider, GitChangesetConverterFactory converterFactory, GitTagConverter gitTagConverter) {
    super(context);
    this.mirrorHttpConnectionProvider = mirrorHttpConnectionProvider;
    this.postReceiveRepositoryHookEventFactory = postReceiveRepositoryHookEventFactory;
    this.converterFactory = converterFactory;
    this.gitTagConverter = gitTagConverter;
  }

  @Override
  public MirrorCommandResult mirror(MirrorCommandRequest mirrorCommandRequest) {
    return update(mirrorCommandRequest);
  }

  @Override
  public MirrorCommandResult update(MirrorCommandRequest mirrorCommandRequest) {
    try (Repository repository = context.open(); Git git = Git.wrap(repository)) {
      return new Worker(mirrorCommandRequest, repository, git).update();
    } catch (IOException e) {
      throw new InternalRepositoryException(context.getRepository(), "error during git fetch", e);
    }
  }

  private class Worker {

    private final MirrorCommandRequest mirrorCommandRequest;
    private final List<String> mirrorLog = new ArrayList<>();
    private final Stopwatch stopwatch;

    private final Repository repository;
    private final Git git;

    private FetchResult fetchResult;
    private GitFilterContext filterContext;
    private MirrorFilter.Filter filter;

    private ResultType result = OK;

    private Worker(MirrorCommandRequest mirrorCommandRequest, Repository repository, Git git) {
      this.mirrorCommandRequest = mirrorCommandRequest;
      this.repository = repository;
      this.git = git;
      stopwatch = Stopwatch.createStarted();
    }

    MirrorCommandResult update() {
      try {
        return doUpdate();
      } catch (GitAPIException e) {
        result = FAILED;
        mirrorLog.add("failed to synchronize: " + e.getMessage());
        return new MirrorCommandResult(FAILED, mirrorLog, stopwatch.stop().elapsed());
      }
    }

    private MirrorCommandResult doUpdate() throws GitAPIException {
      fetchResult = createFetchCommand().call();
      filterContext = new GitFilterContext();
      filter = mirrorCommandRequest.getFilter().getFilter(filterContext);

      if (fetchResult.getTrackingRefUpdates().isEmpty()) {
        mirrorLog.add("No updates found");
      } else {
        handleBranches();
        handleTags();
      }

      postReceiveRepositoryHookEventFactory.fireForFetch(git, fetchResult);
      return new MirrorCommandResult(result, mirrorLog, stopwatch.stop().elapsed());
    }

    private void handleBranches() {
      LoggerWithHeader logger = new LoggerWithHeader("Branches:");
      doForEachRefStartingWith(MIRROR_REF_PREFIX + "heads", ref -> handleBranch(logger, ref));
    }

    private void handleBranch(LoggerWithHeader logger, TrackingRefUpdate ref) {
      MirrorReferenceUpdateHandler refHandler = new MirrorReferenceUpdateHandler(logger, ref, "heads/", "branch");
      refHandler.handleRef(ref1 -> refHandler.testFilterForBranch());
    }

    private void handleTags() {
      LoggerWithHeader logger = new LoggerWithHeader("Tags:");
      doForEachRefStartingWith(MIRROR_REF_PREFIX + "tags", ref -> handleTag(logger, ref));
    }

    private void handleTag(LoggerWithHeader logger, TrackingRefUpdate ref) {
      MirrorReferenceUpdateHandler refHandler = new MirrorReferenceUpdateHandler(logger, ref, "tags/", "tag");
      refHandler.handleRef(ref1 -> refHandler.testFilterForTag());
    }

    private class MirrorReferenceUpdateHandler {
      private final LoggerWithHeader logger;
      private final TrackingRefUpdate ref;
      private final String refType;
      private final String typeForLog;

      public MirrorReferenceUpdateHandler(LoggerWithHeader logger, TrackingRefUpdate ref, String refType, String typeForLog) {
        this.logger = logger;
        this.ref = ref;
        this.refType = refType;
        this.typeForLog = typeForLog;
      }

      private void handleRef(Function<TrackingRefUpdate, Result> filter) {
        Result filterResult = filter.apply(ref);
        try {
          String referenceName = ref.getLocalName().substring(MIRROR_REF_PREFIX.length() + refType.length());
          if (filterResult.isAccepted()) {
            handleAcceptedReference(referenceName);
          } else {
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
        LOG.trace("{} ref rejected in {}: {}", typeForLog, GitMirrorCommand.this.repository, ref.getLocalName());
        if (ref.getResult() == NEW) {
          deleteReference(ref.getLocalName());
        } else {
          updateReference(ref.getLocalName(), ref.getOldObjectId());
        }
        logger.logChange(ref, referenceName, filterResult.getRejectReason().orElse("rejected due to filter"));
      }

      private void handleAcceptedReference(String referenceName) throws IOException {
        String targetRef = "refs/" + refType + referenceName;
        if (isDeletedReference(ref)) {
          LOG.trace("deleting {} ref in {}: {}", typeForLog, GitMirrorCommand.this.repository, targetRef);
          deleteReference(targetRef);
          logger.logChange(ref, referenceName, "deleted");
        } else {
          LOG.trace("updating {} ref in {}: {}", typeForLog, GitMirrorCommand.this.repository, targetRef);
          updateReference(targetRef, ref.getNewObjectId());
          logger.logChange(ref, referenceName, getUpdateType(ref));
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
        RefUpdate deleteUpdate = repository.getRefDatabase().newUpdate(targetRef, true);
        deleteUpdate.setForceUpdate(true);
        deleteUpdate.delete();
      }

      private boolean isDeletedReference(TrackingRefUpdate ref) {
        return ref.asReceiveCommand().getType() == ReceiveCommand.Type.DELETE;
      }

      private void updateReference(String reference, ObjectId objectId) throws IOException {
        LOG.trace("updating ref in {}: {} -> {}", GitMirrorCommand.this.repository, reference, objectId);
        RefUpdate refUpdate = repository.getRefDatabase().newUpdate(reference, true);
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
      FetchCommand fetchCommand = Git.wrap(repository).fetch()
        .setRefSpecs("refs/heads/*:" + MIRROR_REF_PREFIX + "heads/*", "refs/tags/*:" + MIRROR_REF_PREFIX + "tags/*")
        .setForceUpdate(true)
        .setRemoveDeletedRefs(true)
        .setRemote(mirrorCommandRequest.getSourceUrl());

      mirrorCommandRequest.getCredential(Pkcs12ClientCertificateCredential.class)
        .ifPresent(c -> fetchCommand.setTransportConfigCallback(transport -> {
          if (transport instanceof TransportHttp) {
            TransportHttp transportHttp = (TransportHttp) transport;
            transportHttp.setHttpConnectionFactory(mirrorHttpConnectionProvider.createHttpConnectionFactory(c, mirrorLog));
          }
        }));
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
          if (refUpdate.getLocalName().startsWith(MIRROR_REF_PREFIX + "heads")) {
            extractedBranchUpdates.put(refUpdate.getLocalName(), new GitBranchUpdate(refUpdate));
          }
          if (refUpdate.getLocalName().startsWith(MIRROR_REF_PREFIX + "tags")) {
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
        this.branchName = refUpdate.getLocalName().substring(MIRROR_REF_PREFIX.length() + "heads/".length());
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
        try (RevWalk revWalk = new RevWalk(repository); GitChangesetConverter gitChangesetConverter = converter(revWalk)) {
          try {
            RevCommit revCommit = revWalk.parseCommit(refUpdate.getNewObjectId());
            return gitChangesetConverter.createChangeset(revCommit, refUpdate.getLocalName());
          } catch (Exception e) {
            throw new InternalRepositoryException(context.getRepository(), "got exception while validating branch", e);
          }
        }
    }

    private GitChangesetConverter converter(RevWalk revWalk) {
      return converterFactory.builder(repository)
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
        this.tagName = refUpdate.getLocalName().substring(MIRROR_REF_PREFIX.length() + "tags/".length());
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
        try (RevWalk revWalk = new RevWalk(repository)) {
          try {
            RevObject revObject = revWalk.parseAny(refUpdate.getNewObjectId());
            if (revObject.getType() == Constants.OBJ_TAG) {
              RevTag revTag = revWalk.parseTag(revObject.getId());
              return gitTagConverter.buildTag(revTag, revWalk);
            } else if (revObject.getType() == Constants.OBJ_COMMIT) {
              Ref ref = repository.getRefDatabase().findRef(refUpdate.getLocalName());
              Tag t = gitTagConverter.buildTag(repository, revWalk, ref);
              return new Tag(tagName, t.getRevision(), t.getDate().orElse(null), t.getDeletable());
            } else {
              throw new InternalRepositoryException(context.getRepository(), "invalid object type for tag");
            }
          } catch (Exception e) {
            throw new InternalRepositoryException(context.getRepository(), "got exception while validating tag", e);
          }
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
}
