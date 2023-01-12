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
