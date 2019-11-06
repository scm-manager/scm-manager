package sonia.scm.repository.spi;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand.FastForwardMode;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.ResolveMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.GitWorkdirFactory;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Person;
import sonia.scm.repository.api.MergeCommandResult;
import sonia.scm.repository.api.MergeDryRunCommandResult;
import sonia.scm.repository.api.MergeStrategy;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Set;

public class GitMergeCommand extends AbstractGitCommand implements MergeCommand {

  private static final Logger logger = LoggerFactory.getLogger(GitMergeCommand.class);

  private static final String MERGE_COMMIT_MESSAGE_TEMPLATE = String.join("\n",
    "Merge of branch {0} into {1}",
    "",
    "Automatic merge by SCM-Manager.");

  private final GitWorkdirFactory workdirFactory;

  private static final Set<MergeStrategy> STRATEGIES = ImmutableSet.of(
    MergeStrategy.SQUASH,
    MergeStrategy.MERGE_COMMIT,
    MergeStrategy.FAST_FORWARD_IF_POSSIBLE
  );

  GitMergeCommand(GitContext context, sonia.scm.repository.Repository repository, GitWorkdirFactory workdirFactory) {
    super(context, repository);
    this.workdirFactory = workdirFactory;
  }

  @Override
  public MergeCommandResult merge(MergeCommandRequest request) {
    return inClone(clone -> new MergeWorker(clone, request), workdirFactory, request.getTargetBranch());
  }

  @Override
  public MergeDryRunCommandResult dryRun(MergeCommandRequest request) {
    try {
      Repository repository = context.open();
      ResolveMerger merger = (ResolveMerger) org.eclipse.jgit.merge.MergeStrategy.RECURSIVE.newMerger(repository, true);
      return new MergeDryRunCommandResult(
        merger.merge(
          resolveRevisionOrThrowNotFound(repository, request.getBranchToMerge()),
          resolveRevisionOrThrowNotFound(repository, request.getTargetBranch())));
    } catch (IOException e) {
      throw new InternalRepositoryException(context.getRepository(), "could not clone repository for merge", e);
    }
  }

  @Override
  public boolean isSupported(MergeStrategy strategy) {
    return STRATEGIES.contains(strategy);
  }

  @Override
  public Set<MergeStrategy> getSupportedMergeStrategies() {
    return STRATEGIES;
  }

  private class MergeWorker extends GitCloneWorker<MergeCommandResult> {

    private final String target;
    private final String toMerge;
    private final Person author;
    private final String messageTemplate;
    private final MergeStrategy mergeStrategy;

    private MergeWorker(Git clone, MergeCommandRequest request) {
      super(clone);
      this.target = request.getTargetBranch();
      this.toMerge = request.getBranchToMerge();
      this.author = request.getAuthor();
      this.messageTemplate = request.getMessageTemplate();
      this.mergeStrategy = request.getMergeStrategy();
    }

    @Override
    MergeCommandResult run() throws IOException {
      MergeResult result = doMergeInClone();
      if (result.getMergeStatus().isSuccessful()) {
        doCommit();
        push();
        return MergeCommandResult.success();
      } else {
        return analyseFailure(result);
      }
    }

    private MergeResult doMergeInClone() throws IOException {
      MergeResult result;
      try {
        ObjectId sourceRevision = resolveRevision(toMerge);
        org.eclipse.jgit.api.MergeCommand mergeCommand = getClone().merge();
        mergeCommand
          .setCommit(false) // we want to set the author manually
          .include(toMerge, sourceRevision);

        if (mergeStrategy == MergeStrategy.SQUASH) {
          mergeCommand.setSquash(true);
        } else {
          mergeCommand.setFastForward(FastForwardMode.NO_FF);
        }

        result = mergeCommand.call();
      } catch (GitAPIException e) {
        throw new InternalRepositoryException(context.getRepository(), "could not merge branch " + toMerge + " into " + target, e);
      }
      return result;
    }

    private void doCommit() {
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

    private MergeCommandResult analyseFailure(MergeResult result) {
      logger.info("could not merged branch {} into {} due to conflict in paths {}", toMerge, target, result.getConflicts().keySet());
      return MergeCommandResult.failure(result.getConflicts().keySet());
    }
  }
}
