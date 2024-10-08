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

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.MergeCommandResult;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

public class GitMergeRebase extends GitMergeStrategy {

  private static final Logger logger = LoggerFactory.getLogger(GitMergeRebase.class);

  private final MergeCommandRequest request;

  GitMergeRebase(Git clone, MergeCommandRequest request, GitContext context, Repository repository) {
    super(clone, request, context, repository);
    this.request = request;
  }

  @Override
  MergeCommandResult run() throws IOException {
    RebaseResult result;
    String branchToMerge = request.getBranchToMerge();
    String targetBranch = request.getTargetBranch();
    try {
      checkOutBranch(branchToMerge);
      result =
        getClone()
          .rebase()
          .setUpstream(targetBranch)
          .call();
    } catch (GitAPIException e) {
      throw new InternalRepositoryException(getContext().getRepository(), "could not rebase branch " + branchToMerge + " onto " + targetBranch, e);
    }

    if (result.getStatus().isSuccessful()) {
      return fastForwardTargetBranch(branchToMerge, targetBranch, result);
    } else {
      logger.info("could not rebase branch {} into {} with rebase status '{}' due to ...", branchToMerge, targetBranch, result.getStatus());
      logger.info("... conflicts: {}", result.getConflicts());
      logger.info("... failing paths: {}", result.getFailingPaths());
      logger.info("... message: {}", result);
      return MergeCommandResult.failure(branchToMerge, targetBranch, Optional.ofNullable(result.getConflicts()).orElse(Collections.singletonList("UNKNOWN")));
    }
  }

  private MergeCommandResult fastForwardTargetBranch(String branchToMerge, String targetBranch, RebaseResult result) throws IOException {
    try {
      getClone().checkout().setName(targetBranch).call();
      ObjectId sourceRevision = resolveRevision(branchToMerge);
      getClone()
        .merge()
        .setFastForward(MergeCommand.FastForwardMode.FF_ONLY)
        .include(branchToMerge, sourceRevision)
        .call();
      push();
      return createSuccessResult(sourceRevision.name());
    } catch (GitAPIException e) {
      return MergeCommandResult.failure(branchToMerge, targetBranch, result.getConflicts());
    }

  }
}
