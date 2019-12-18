package sonia.scm.repository.spi;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.revwalk.RevCommit;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.MergeCommandResult;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import static sonia.scm.repository.spi.GitRevisionExtractor.extractRevisionFromRevCommit;

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
      Optional<RevCommit> revCommit = doCommit();
      push();
      return new MergeCommandResult(Collections.emptyList(), extractRevisionFromRevCommit(revCommit));
    } else {
      return analyseFailure(result);
    }
  }

}
