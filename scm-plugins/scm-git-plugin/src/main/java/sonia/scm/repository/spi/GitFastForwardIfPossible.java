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
import org.eclipse.jgit.lib.ObjectId;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.api.MergeCommandResult;

@Slf4j
class GitFastForwardIfPossible {

  private final MergeCommandRequest request;
  private final MergeHelper mergeHelper;
  private final GitMergeCommit fallbackMerge;
  private final CommitHelper commitHelper;
  private final Repository repository;

  GitFastForwardIfPossible(MergeCommandRequest request, GitContext context, RepositoryManager repositoryManager, GitRepositoryHookEventFactory eventFactory) {
    this.request = request;
    this.mergeHelper = new MergeHelper(context, request, repositoryManager, eventFactory);
    this.fallbackMerge = new GitMergeCommit(request, context, repositoryManager, eventFactory);
    this.commitHelper = new CommitHelper(context, repositoryManager, eventFactory);
    this.repository = context.getRepository();
  }

  MergeCommandResult run() {
    log.trace("try to fast forward branch {} onto {} in repository {}", request.getBranchToMerge(), request.getTargetBranch(), repository);
    ObjectId sourceRevision = mergeHelper.getRevisionToMerge();
    ObjectId targetRevision = mergeHelper.getTargetRevision();

    if (mergeHelper.isMergedInto(targetRevision, sourceRevision)) {
      log.trace("fast forward branch {} onto {}", request.getBranchToMerge(), request.getTargetBranch());
      commitHelper.updateBranch(request.getTargetBranch(), sourceRevision, targetRevision);
      return MergeCommandResult.success(targetRevision.name(), mergeHelper.getRevisionToMerge().name(), sourceRevision.name());
    } else {
      log.trace("fast forward is not possible, fallback to merge");
      return fallbackMerge.run();
    }
  }
}
