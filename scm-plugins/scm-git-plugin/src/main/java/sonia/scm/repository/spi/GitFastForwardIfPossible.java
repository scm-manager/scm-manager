package sonia.scm.repository.spi;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.MergeCommandResult;

import java.io.IOException;

class GitFastForwardIfPossible extends GitMergeStrategy {

  GitFastForwardIfPossible(Git clone, MergeCommandRequest request, GitContext context, Repository repository) {
    super(clone, request, context, repository);
  }

  @Override
  MergeCommandResult run() throws IOException {
    MergeResult fastForwardResult = mergeWithFastForwardMode(MergeCommand.FastForwardMode.FF_ONLY);
    if (fastForwardResult.getMergeStatus().isSuccessful()) {
      push();
      return MergeCommandResult.success();
    } else {
      return mergeWithCommit();
    }
  }

  private MergeCommandResult mergeWithCommit() throws IOException {
    MergeResult mergeCommitResult = mergeWithFastForwardMode(MergeCommand.FastForwardMode.NO_FF);
    if (mergeCommitResult.getMergeStatus().isSuccessful()) {
      doCommit();
      push();
      return MergeCommandResult.success();
    } else {
      return analyseFailure(mergeCommitResult);
    }
  }

  private MergeResult mergeWithFastForwardMode(MergeCommand.FastForwardMode fastForwardMode) throws IOException {
    MergeCommand mergeCommand = getClone().merge();
    mergeCommand.setFastForward(fastForwardMode);
    return doMergeInClone(mergeCommand);
  }
}
