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
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
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
import sonia.scm.repository.api.MirrorFilter;
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

import static java.lang.String.format;
import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.of;
import static org.eclipse.jgit.lib.RefUpdate.Result.NEW;

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

  private List<String> log;

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
    log = new ArrayList<>();
    Stopwatch stopwatch = Stopwatch.createStarted();
    try (Repository repository = context.open(); Git git = Git.wrap(repository)) {
      return doUpdate(mirrorCommandRequest, stopwatch, repository, git);
    } catch (IOException e) {
      throw new InternalRepositoryException(context.getRepository(), "error during git fetch", e);
    } catch (GitAPIException e) {
      log.add("failed to synchronize: " + e.getMessage());
      return new MirrorCommandResult(false, log, stopwatch.stop().elapsed());
    }
  }

  private MirrorCommandResult doUpdate(MirrorCommandRequest mirrorCommandRequest, Stopwatch stopwatch, Repository repository, Git git) throws GitAPIException {
    FetchResult fetchResult = createFetchCommand(mirrorCommandRequest, repository).call();
    GitFilterContext filterContext = new GitFilterContext(fetchResult, repository);
    log.add("Branches:");
    MirrorFilter.Filter filter = mirrorCommandRequest.getFilter().getFilter(filterContext);
    doForEachRefStartingWith(fetchResult, MIRROR_REF_PREFIX + "heads", ref -> {
      String branchName = ref.getLocalName().substring(MIRROR_REF_PREFIX.length() + "heads/".length());
      if (filter.acceptBranch(filterContext.getBranchUpdate(ref.getLocalName()))) {
        String targetRef = "refs/heads" + ref.getLocalName().substring(MIRROR_REF_PREFIX.length() + "heads".length());
        if (ref.asReceiveCommand().getType() == ReceiveCommand.Type.DELETE) {
          String reference = "refs/heads/" + branchName;
          LOG.trace("deleting ref in {}: {}", repository, reference);
          updateReference(repository, targetRef, ref.getNewObjectId()); // without this, the following deletion might be rejected
          repository.getRefDatabase().newUpdate(targetRef, true).delete();
          log.add(
            format("- %s..%s %s (deleted)",
              ref.getOldObjectId().abbreviate(9).name(),
              ref.getNewObjectId().abbreviate(9).name(),
              branchName
            ));
        } else {
          updateReference(repository, targetRef, ref.getNewObjectId());
          log.add(
            format("- %s..%s %s (%s)",
              ref.getOldObjectId().abbreviate(9).name(),
              ref.getNewObjectId().abbreviate(9).name(),
              branchName,
              getUpdateType(ref)
            ));
        }
      } else {
        updateReference(repository, ref.getLocalName(), ref.getOldObjectId());
        log.add(
          format("- %s..%s %s (rejected due to filter)",
            ref.getOldObjectId().abbreviate(9).name(),
            ref.getNewObjectId().abbreviate(9).name(),
            branchName
          ));
      }
    });
    log.add("Tags:");
    doForEachRefStartingWith(fetchResult, MIRROR_REF_PREFIX + "tags", ref -> {
      String tagName = ref.getLocalName().substring(MIRROR_REF_PREFIX.length() + "tags/".length());
      if (filter.acceptTag(filterContext.getTagUpdate(ref.getLocalName()))) {
        String targetRef = "refs/tags/" + tagName;
        if (ref.asReceiveCommand().getType() == ReceiveCommand.Type.DELETE) {
          updateReference(repository, targetRef, ref.getNewObjectId()); // without this, the following deletion might be rejected
          repository.getRefDatabase().newUpdate(targetRef, true).delete();
          log.add(
            format("- %s..%s %s (deleted)",
              ref.getOldObjectId().abbreviate(9).name(),
              ref.getNewObjectId().abbreviate(9).name(),
              tagName
            ));
      } else {
          updateReference(repository, targetRef, ref.getNewObjectId());
          log.add(
            format("- %s..%s %s (%s)",
              ref.getOldObjectId().abbreviate(9).name(),
              ref.getNewObjectId().abbreviate(9).name(),
              tagName,
              getUpdateType(ref)
            ));
        }
      } else {
        if (ref.getResult() != NEW) {
          updateReference(repository, ref.getLocalName(), ref.getOldObjectId());
        }
        log.add(
          format("- %s..%s %s (rejected due to filter)",
            ref.getOldObjectId().abbreviate(9).name(),
            ref.getNewObjectId().abbreviate(9).name(),
            tagName
          ));
      }
    });
    postReceiveRepositoryHookEventFactory.fireForFetch(git, fetchResult);
    return new MirrorCommandResult(true, log, stopwatch.stop().elapsed());
  }

  private void doForEachRefStartingWith(FetchResult fetchResult, String prefix, RefUpdateConsumer refUpdateConsumer) {
    fetchResult.getTrackingRefUpdates()
      .stream()
      .filter(ref -> ref.getLocalName().startsWith(prefix))
      .forEach(ref -> {
        try {
          refUpdateConsumer.accept(ref);
        } catch (IOException e) {
          // TODO
          e.printStackTrace();
        }
      });
  }

  private void updateReference(Repository repository, String reference, ObjectId objectId) throws IOException {
    LOG.trace("updating ref in {}: {} -> {}", repository, reference, objectId);
    RefUpdate refUpdate = repository.getRefDatabase().newUpdate(reference, true);
    refUpdate.setNewObjectId(objectId);
    refUpdate.forceUpdate();
  }

  private FetchCommand createFetchCommand(MirrorCommandRequest mirrorCommandRequest, Repository repository) {
    FetchCommand fetchCommand = Git.wrap(repository).fetch()
      .setRefSpecs("refs/heads/*:" + MIRROR_REF_PREFIX + "heads/*", "refs/tags/*:" + MIRROR_REF_PREFIX + "tags/*")
      .setForceUpdate(true)
      .setRemoveDeletedRefs(true)
      .setRemote(mirrorCommandRequest.getSourceUrl());

    mirrorCommandRequest.getCredential(Pkcs12ClientCertificateCredential.class)
      .ifPresent(c -> fetchCommand.setTransportConfigCallback(transport -> {
        if (transport instanceof TransportHttp) {
          TransportHttp transportHttp = (TransportHttp) transport;
          transportHttp.setHttpConnectionFactory(mirrorHttpConnectionProvider.createHttpConnectionFactory(c, log));
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

  private String getUpdateType(TrackingRefUpdate trackingRefUpdate) {
    return trackingRefUpdate.getResult().name().toLowerCase(Locale.ENGLISH);
  }

  private class GitFilterContext implements MirrorFilter.FilterContext {

    private final Map<String, MirrorFilter.BranchUpdate> branchUpdates;
    private final Map<String, MirrorFilter.TagUpdate> tagUpdates;

    public GitFilterContext(FetchResult fetchResult, Repository repository) {
      Map<String, MirrorFilter.BranchUpdate> branchUpdates = new HashMap<>();
      Map<String, MirrorFilter.TagUpdate> tagUpdates = new HashMap<>();

      fetchResult.getTrackingRefUpdates().forEach(refUpdate -> {
        if (refUpdate.getLocalName().startsWith(MIRROR_REF_PREFIX + "heads")) {
          branchUpdates.put(refUpdate.getLocalName(), new GitBranchUpdate(refUpdate, repository));
        }
        if (refUpdate.getLocalName().startsWith(MIRROR_REF_PREFIX + "tags")) {
          tagUpdates.put(refUpdate.getLocalName(), new GitTagUpdate(refUpdate, repository));
        }
      });

      this.branchUpdates = unmodifiableMap(branchUpdates);
      this.tagUpdates = unmodifiableMap(tagUpdates);
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

    public MirrorFilter.TagUpdate getTagUpdate(String ref) {
      return tagUpdates.get(ref);
    }
  }

  private class GitBranchUpdate implements MirrorFilter.BranchUpdate {

    private final TrackingRefUpdate refUpdate;
    private final Repository repository;

    private final String branchName;

    private Changeset changeset;

    public GitBranchUpdate(TrackingRefUpdate refUpdate, Repository repository) {
      this.refUpdate = refUpdate;
      this.repository = repository;

      this.branchName = refUpdate.getLocalName().substring(MIRROR_REF_PREFIX.length() + "heads/".length());
    }

    @Override
    public String getBranchName() {
      return branchName;
    }

    @Override
    public Changeset getChangeset() {
      if (changeset == null) {
        changeset = computeChangeset();
      }
      return changeset;
    }

    @Override
    public String getNewRevision() {
      return refUpdate.getNewObjectId().name();
    }

    @Override
    public Optional<String> getOldRevision() {
      return of(refUpdate.getOldObjectId().name());
    }

    @Override
    public boolean isForcedUpdate() {
      return refUpdate.getResult() == RefUpdate.Result.FORCED;
    }

    private Changeset computeChangeset() {
      try (RevWalk revWalk = new RevWalk(repository); GitChangesetConverter gitChangesetConverter = converterFactory.create(repository, revWalk)) {
        try {
          RevCommit revCommit = revWalk.parseCommit(refUpdate.getNewObjectId());
          return gitChangesetConverter.createChangeset(revCommit, refUpdate.getLocalName());
        } catch (Exception e) {
          throw new InternalRepositoryException(context.getRepository(), "got exception while validating branch", e);
        }
      }
    }
  }

  private class GitTagUpdate implements MirrorFilter.TagUpdate {

    private final TrackingRefUpdate refUpdate;
    private final Repository repository;

    private final String tagName;

    private Tag tag;

    public GitTagUpdate(TrackingRefUpdate refUpdate, Repository repository) {
      this.refUpdate = refUpdate;
      this.repository = repository;

      this.tagName = refUpdate.getLocalName().substring(MIRROR_REF_PREFIX.length() + "tags/".length());
    }

    @Override
    public String getTagName() {
      return tagName;
    }

    @Override
    public Tag getTag() {
      if (tag == null) {
        tag = computeTag();
      }
      return tag;
    }

    @Override
    public Optional<String> getOldRevision() {
      return Optional.empty();
    }

    private Tag computeTag() {
      try (RevWalk revWalk = new RevWalk(repository)) {
        try {
          return gitTagConverter.buildTag(repository, revWalk, refUpdate.asReceiveCommand().getRef());
        } catch (Exception e) {
          throw new InternalRepositoryException(context.getRepository(), "got exception while validating tag", e);
        }
      }
    }
  }

  private interface RefUpdateConsumer {
    void accept(TrackingRefUpdate refUpdate) throws IOException;
  }
}
