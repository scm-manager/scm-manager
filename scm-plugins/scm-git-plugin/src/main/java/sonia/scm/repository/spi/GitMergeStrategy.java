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

import com.google.common.base.Strings;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Person;
import sonia.scm.repository.api.MergeCommandResult;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Optional;

abstract class GitMergeStrategy extends AbstractGitCommand.GitCloneWorker<MergeCommandResult> {

  private static final Logger logger = LoggerFactory.getLogger(GitMergeStrategy.class);

  private static final String MERGE_COMMIT_MESSAGE_TEMPLATE = String.join("\n",
    "Merge of branch {0} into {1}",
    "",
    "Automatic merge by SCM-Manager.");

  private final String targetBranch;
  private final ObjectId targetRevision;
  private final String branchToMerge;
  private final ObjectId revisionToMerge;
  private final Person author;
  private final String messageTemplate;
  private final boolean sign;

  GitMergeStrategy(Git clone, MergeCommandRequest request, GitContext context, sonia.scm.repository.Repository repository) {
    super(clone, context, repository);
    this.targetBranch = request.getTargetBranch();
    this.branchToMerge = request.getBranchToMerge();
    this.author = request.getAuthor();
    this.messageTemplate = request.getMessageTemplate();
    this.sign = request.isSign();
    try {
      this.targetRevision = resolveRevision(request.getTargetBranch());
      this.revisionToMerge = resolveRevision(request.getBranchToMerge());
    } catch (IOException e) {
      throw new InternalRepositoryException(repository, "Could not resolve revisions of target branch or branch to merge", e);
    }
  }

  MergeResult doMergeInClone(MergeCommand mergeCommand) throws IOException {
    MergeResult result;
    try {
      ObjectId sourceRevision = resolveRevision(branchToMerge);
      mergeCommand
        .setCommit(false) // we want to set the author manually
        .include(branchToMerge, sourceRevision);

      result = mergeCommand.call();
    } catch (GitAPIException e) {
      throw new InternalRepositoryException(getContext().getRepository(), "could not merge branch " + branchToMerge + " into " + targetBranch, e);
    }
    return result;
  }

  Optional<RevCommit> doCommit() {
    logger.debug("merged branch {} into {}", branchToMerge, targetBranch);
    return doCommit(MessageFormat.format(determineMessageTemplate(), branchToMerge, targetBranch), author, sign);
  }

  MergeCommandResult createSuccessResult(String newRevision) {
    return MergeCommandResult.success(targetRevision.name(), revisionToMerge.name(), newRevision);
  }

  ObjectId getTargetRevision() {
    return targetRevision;
  }

  ObjectId getRevisionToMerge() {
    return revisionToMerge;
  }

  private String determineMessageTemplate() {
    if (Strings.isNullOrEmpty(messageTemplate)) {
      return MERGE_COMMIT_MESSAGE_TEMPLATE;
    } else {
      return messageTemplate;
    }
  }

  MergeCommandResult analyseFailure(MergeResult result) {
    logger.info("could not merge branch {} into {} with merge status '{}' due to ...", branchToMerge, targetBranch, result.getMergeStatus());
    logger.info("... conflicts: {}", result.getConflicts());
    logger.info("... checkout conflicts: {}", result.getCheckoutConflicts());
    logger.info("... failing paths: {}", result.getFailingPaths());
    logger.info("... message: {}", result);
    if (result.getConflicts() == null) {
      throw new UnexpectedMergeResultException(getRepository(), result);
    }
    return MergeCommandResult.failure(targetRevision.name(), revisionToMerge.name(), result.getConflicts().keySet());
  }
}
