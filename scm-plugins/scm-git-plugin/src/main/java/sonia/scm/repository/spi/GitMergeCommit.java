package sonia.scm.repository.spi;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.MergeCommandResult;

import java.io.IOException;

class GitMergeCommit extends GitMergeStrategy {

  GitMergeCommit(Git clone, MergeCommandRequest request, GitContext context, Repository repository) {
    super(clone, request, context, repository);
  }

  @Override
  MergeCommandResult run() throws IOException {
    MergeCommand mergeCommand = getClone().merge();
    mergeCommand.setFastForward(MergeCommand.FastForwardMode.NO_FF);
    MergeResult result = doMergeInClone(mergeCommand);

    if (result.getMergeStatus().isSuccessful()) {
      doCommit();
      push();
      return MergeCommandResult.success();
    } else {
      return analyseFailure(result);
    }
  }
}
