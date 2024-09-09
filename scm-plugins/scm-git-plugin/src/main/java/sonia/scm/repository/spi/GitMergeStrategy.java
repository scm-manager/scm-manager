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
  private final String message;
  private final boolean sign;

  GitMergeStrategy(Git clone, MergeCommandRequest request, GitContext context, sonia.scm.repository.Repository repository) {
    super(clone, context, repository);
    this.targetBranch = request.getTargetBranch();
    this.branchToMerge = request.getBranchToMerge();
    this.author = request.getAuthor();
    this.messageTemplate = request.getMessageTemplate();
    this.message = request.getMessage();
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
    return doCommit(determineMessage(), author, sign);
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

  private String determineMessage() {
    if (!Strings.isNullOrEmpty(message)) {
      return message;
    } else if (!Strings.isNullOrEmpty(messageTemplate)) {
      return MessageFormat.format(messageTemplate, branchToMerge, targetBranch);
    } else {
      return MessageFormat.format(MERGE_COMMIT_MESSAGE_TEMPLATE, branchToMerge, targetBranch);
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
