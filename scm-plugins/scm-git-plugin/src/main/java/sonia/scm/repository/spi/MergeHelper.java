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

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.UnsupportedSigningFormatException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.ResolveMerger;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import sonia.scm.NoChangesMadeException;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Person;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.api.MergeCommandResult;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;

import static org.eclipse.jgit.merge.MergeStrategy.RECURSIVE;
import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

@Slf4j
class MergeHelper {

  private static final String MERGE_COMMIT_MESSAGE_TEMPLATE = String.join("\n",
    "Merge of branch {0} into {1}",
    "",
    "Automatic merge by SCM-Manager.");

  private final GitContext context;
  private final RepositoryManager repositoryManager;
  private final GitRepositoryHookEventFactory eventFactory;
  private final Repository repository;

  private final ObjectId targetRevision;
  private final ObjectId revisionToMerge;
  private final String targetBranch;
  private final String branchToMerge;
  private final String messageTemplate;
  private final String message;

  MergeHelper(GitContext context,
              MergeCommandRequest request,
              RepositoryManager repositoryManager,
              GitRepositoryHookEventFactory eventFactory) {
    this.context = context;
    this.repositoryManager = repositoryManager;
    this.eventFactory = eventFactory;
    try {
      this.repository = context.open();
      this.targetRevision = resolveRevision(request.getTargetBranch());
      this.revisionToMerge = resolveRevision(request.getBranchToMerge());
    } catch (IOException e) {
      throw new InternalRepositoryException(context.getRepository(), "Could not resolve revisions of target branch or branch to merge", e);
    }
    this.targetBranch = request.getTargetBranch();
    this.branchToMerge = request.getBranchToMerge();
    this.messageTemplate = request.getMessageTemplate();
    this.message = request.getMessage();
  }

  static Collection<String> getFailingPaths(ResolveMerger merger) {
    return merger.getMergeResults()
      .entrySet()
      .stream()
      .filter(entry -> entry.getValue().containsConflicts())
      .map(Map.Entry::getKey)
      .toList();
  }

  ObjectId getTargetRevision() {
    return targetRevision;
  }

  ObjectId getRevisionToMerge() {
    return revisionToMerge;
  }

  ObjectId resolveRevision(String revision) throws IOException {
    ObjectId resolved = repository.resolve(revision);
    if (resolved == null) {
      throw notFound(entity("Revision", revision).in(context.getRepository()));
    } else {
      return resolved;
    }
  }

  String determineMessage() {
    if (!Strings.isNullOrEmpty(message)) {
      return message;
    } else if (!Strings.isNullOrEmpty(messageTemplate)) {
      return MessageFormat.format(messageTemplate, branchToMerge, targetBranch);
    } else {
      return MessageFormat.format(MERGE_COMMIT_MESSAGE_TEMPLATE, branchToMerge, targetBranch);
    }
  }

  boolean isMergedInto(ObjectId baseRevision, ObjectId revisionToCheck) {
    try (RevWalk revWalk = new RevWalk(context.open())) {
      RevCommit baseCommit = revWalk.parseCommit(baseRevision);
      RevCommit commitToCheck = revWalk.parseCommit(revisionToCheck);
      return revWalk.isMergedInto(baseCommit, commitToCheck);
    } catch (IOException e) {
      throw new InternalRepositoryException(context.getRepository(), "failed to check whether revision " + revisionToCheck + " is merged into " + baseRevision, e);
    }
  }

  MergeCommandResult doRecursiveMerge(MergeCommandRequest request, BiFunction<ObjectId, ObjectId, ObjectId[]> parents) {
    return doRecursiveMerge(request, request.getAuthor(), parents);
  }

  MergeCommandResult doRecursiveMerge(MergeCommandRequest request, Person committer, BiFunction<ObjectId, ObjectId, ObjectId[]> parents) {
    log.trace("merge branch {} into {}", branchToMerge, targetBranch);
    try {
      org.eclipse.jgit.lib.Repository repository = context.open();
      ObjectId sourceRevision = getRevisionToMerge();
      ObjectId targetRevision = getTargetRevision();

      assertBranchesNotMerged(request, sourceRevision, targetRevision);

      ResolveMerger merger = (ResolveMerger) RECURSIVE.newMerger(repository, true); // The recursive merger is always a RecursiveMerge
      boolean mergeSucceeded = merger.merge(sourceRevision, targetRevision);
      if (!mergeSucceeded) {
        log.trace("could not merge branch {} into {}", branchToMerge, targetBranch);
        return MergeCommandResult.failure(targetRevision.name(), sourceRevision.name(), getFailingPaths(merger));
      }
      ObjectId newTreeId = merger.getResultTreeId();
      log.trace("create commit for new tree {}", newTreeId);

      CommitHelper commitHelper = new CommitHelper(context, repositoryManager, eventFactory);
      ObjectId commitId = commitHelper.createCommit(
        newTreeId,
        request.getAuthor(),
        committer,
        determineMessage(),
        request.isSign(),
        parents.apply(sourceRevision, targetRevision)
      );
      log.trace("created commit {}", commitId);

      commitHelper.updateBranch(request.getTargetBranch(), commitId, targetRevision);

      return MergeCommandResult.success(targetRevision.name(), sourceRevision.name(), commitId.name());
    } catch (IOException | CanceledException | UnsupportedSigningFormatException e) {
      throw new InternalRepositoryException(context.getRepository(), "Error during merge", e);
    }
  }

  private void assertBranchesNotMerged(MergeCommandRequest request, ObjectId sourceRevision, ObjectId targetRevision) throws IOException {
    if (isMergedInto(sourceRevision, targetRevision)) {
      throw new NoChangesMadeException(context.getRepository(), request.getTargetBranch());
    }
  }
}
