package sonia.scm.repository.spi;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.MergeCommandResult;

import java.io.IOException;

class GitMergeWithSquash extends GitMergeStrategy {

  GitMergeWithSquash(Git clone, MergeCommandRequest request, GitContext context, Repository repository) {
    super(clone, request, context, repository);
  }

  @Override
  MergeCommandResult run() throws IOException {
    org.eclipse.jgit.api.MergeCommand mergeCommand = getClone().merge();
    mergeCommand.setSquash(true);
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
