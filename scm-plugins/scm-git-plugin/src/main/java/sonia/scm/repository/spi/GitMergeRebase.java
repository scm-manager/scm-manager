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

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.UnsupportedSigningFormatException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.merge.ResolveMerger;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Person;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.api.MergeCommandResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static java.util.Optional.ofNullable;
import static org.eclipse.jgit.merge.MergeStrategy.RESOLVE;

@Slf4j
class GitMergeRebase {

  private final MergeCommandRequest request;
  private final GitContext context;
  private final MergeHelper mergeHelper;
  private final CommitHelper commitHelper;
  private final GitFastForwardIfPossible fastForwardMerge;

  GitMergeRebase(MergeCommandRequest request, GitContext context, RepositoryManager repositoryManager, GitRepositoryHookEventFactory eventFactory) {
    this.request = request;
    this.context = context;
    this.mergeHelper = new MergeHelper(context, request, repositoryManager, eventFactory);
    this.commitHelper = new CommitHelper(context, repositoryManager, eventFactory);
    this.fastForwardMerge = new GitFastForwardIfPossible(request, context, repositoryManager, eventFactory);
  }

  MergeCommandResult run() {
    log.debug("rebase branch {} onto {}", request.getBranchToMerge(), request.getTargetBranch());

    ObjectId sourceRevision = mergeHelper.getRevisionToMerge();
    ObjectId targetRevision = mergeHelper.getTargetRevision();
    if (mergeHelper.isMergedInto(targetRevision, sourceRevision)) {
      log.trace("fast forward is possible; using fast forward merge");
      return fastForwardMerge.run();
    }

    try {
      List<RevCommit> commits = computeCommits();
      Collections.reverse(commits);

      for (RevCommit commit : commits) {
        log.trace("rebase {} onto {}", commit, targetRevision);
        ResolveMerger merger = (ResolveMerger) RESOLVE.newMerger(context.open(), true); // The recursive merger is always a RecursiveMerge
        merger.setBase(commit.getParent(0));
        boolean mergeSucceeded = merger.merge(commit, targetRevision);
        if (!mergeSucceeded) {
          log.trace("could not merge {} into {}", commit, targetRevision);
          return MergeCommandResult.failure(request.getBranchToMerge(), request.getTargetBranch(), ofNullable(merger.getUnmergedPaths()).orElse(Collections.singletonList("UNKNOWN")));
        }
        ObjectId newTreeId = merger.getResultTreeId();
        log.trace("create commit for new tree {}", newTreeId);

        PersonIdent originalAuthor = commit.getAuthorIdent();
        targetRevision = commitHelper.createCommit(
          newTreeId,
          originalAuthor,
          request.getAuthor(),
          commit.getFullMessage(),
          request.isSign(),
          targetRevision
        );
        log.trace("created {}", targetRevision);
      }
      log.trace("update branch {} to new revision {}", request.getTargetBranch(), targetRevision);
      commitHelper.updateBranch(request.getTargetBranch(), targetRevision, mergeHelper.getTargetRevision());
      return MergeCommandResult.success(targetRevision.name(), mergeHelper.getRevisionToMerge().name(), targetRevision.name());
    } catch (IOException | CanceledException | UnsupportedSigningFormatException e) {
      throw new InternalRepositoryException(context.getRepository(), "could not rebase branch " + request.getBranchToMerge() + " onto " + request.getTargetBranch(), e);
    }
  }

  private List<RevCommit> computeCommits() throws IOException {
    List<RevCommit> cherryPickList = new ArrayList<>();
    try (RevWalk revWalk = new RevWalk(context.open())) {
      revWalk.sort(RevSort.TOPO_KEEP_BRANCH_TOGETHER, true);
      revWalk.sort(RevSort.COMMIT_TIME_DESC, true);
      revWalk.markUninteresting(revWalk.lookupCommit(mergeHelper.getTargetRevision()));
      revWalk.markStart(revWalk.lookupCommit(mergeHelper.getRevisionToMerge()));

      for (RevCommit commit : revWalk) {
        if (commit.getParentCount() <= 1) {
          log.trace("add {} to cherry pick list", commit);
          cherryPickList.add(commit);
        } else {
          log.trace("skip {} because it has more than one parent", commit);
        }
      }
    }
    return cherryPickList;
  }
}
