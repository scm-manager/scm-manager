package sonia.scm.repository.spi;

import com.google.common.base.Strings;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Person;
import sonia.scm.repository.api.MergeCommandResult;

import java.io.IOException;
import java.text.MessageFormat;

abstract class GitMergeStrategy extends AbstractGitCommand.GitCloneWorker<MergeCommandResult> {

  private static final Logger logger = LoggerFactory.getLogger(GitMergeStrategy.class);

  private static final String MERGE_COMMIT_MESSAGE_TEMPLATE = String.join("\n",
    "Merge of branch {0} into {1}",
    "",
    "Automatic merge by SCM-Manager.");

  private final String target;
  private final String toMerge;
  private final Person author;
  private final String messageTemplate;

  GitMergeStrategy(Git clone, MergeCommandRequest request, GitContext context, sonia.scm.repository.Repository repository) {
    super(clone, context, repository);
    this.target = request.getTargetBranch();
    this.toMerge = request.getBranchToMerge();
    this.author = request.getAuthor();
    this.messageTemplate = request.getMessageTemplate();
  }

  MergeResult doMergeInClone(MergeCommand mergeCommand) throws IOException {
    MergeResult result;
    try {
      ObjectId sourceRevision = resolveRevision(toMerge);
      mergeCommand
        .setCommit(false) // we want to set the author manually
        .include(toMerge, sourceRevision);

      result = mergeCommand.call();
    } catch (GitAPIException e) {
      throw new InternalRepositoryException(getContext().getRepository(), "could not merge branch " + toMerge + " into " + target, e);
    }
    return result;
  }

  void doCommit() {
    logger.debug("merged branch {} into {}", toMerge, target);
    doCommit(MessageFormat.format(determineMessageTemplate(), toMerge, target), author);
  }

  private String determineMessageTemplate() {
    if (Strings.isNullOrEmpty(messageTemplate)) {
      return MERGE_COMMIT_MESSAGE_TEMPLATE;
    } else {
      return messageTemplate;
    }
  }

  MergeCommandResult analyseFailure(MergeResult result) {
    logger.info("could not merged branch {} into {} due to conflict in paths {}", toMerge, target, result.getConflicts().keySet());
    return MergeCommandResult.failure(result.getConflicts().keySet());
  }
}
