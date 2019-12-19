package sonia.scm.repository.spi;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.MergeCommandResult;

import java.io.IOException;
import java.util.Collections;

class GitFastForwardIfPossible extends GitMergeStrategy {

  private GitMergeStrategy fallbackMerge;

  GitFastForwardIfPossible(Git clone, MergeCommandRequest request, GitContext context, Repository repository) {
    super(clone, request, context, repository);
    fallbackMerge = new GitMergeCommit(clone, request, context, repository);
  }

  @Override
  MergeCommandResult run() throws IOException {
    MergeResult fastForwardResult = mergeWithFastForwardOnlyMode();
    if (fastForwardResult.getMergeStatus().isSuccessful()) {
      push();
      return MergeCommandResult.success(fastForwardResult.getNewHead().toString());
    } else {
      return fallbackMerge.run();
    }
  }

  private MergeResult mergeWithFastForwardOnlyMode() throws IOException {
    MergeCommand mergeCommand = getClone().merge();
    mergeCommand.setFastForward(MergeCommand.FastForwardMode.FF_ONLY);
    return doMergeInClone(mergeCommand);
  }
}
