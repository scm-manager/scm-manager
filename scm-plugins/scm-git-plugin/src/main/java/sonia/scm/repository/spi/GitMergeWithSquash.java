package sonia.scm.repository.spi;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.revwalk.RevCommit;
import sonia.scm.NoChangesMadeException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.MergeCommandResult;

import java.io.IOException;

import static sonia.scm.repository.spi.GitRevisionExtractor.extractRevisionFromRevCommit;

class GitMergeWithSquash extends GitMergeStrategy {

  GitMergeWithSquash(Git clone, MergeCommandRequest request, GitContext context, Repository repository) {
    super(clone, request, context, repository);
  }

  @Override
  MergeCommandResult run() throws IOException {
    MergeCommand mergeCommand = getClone().merge();
    mergeCommand.setSquash(true);
    MergeResult result = doMergeInClone(mergeCommand);

    if (result.getMergeStatus().isSuccessful()) {
      RevCommit revCommit = doCommit().orElseThrow(() -> new NoChangesMadeException(getRepository()));
      push();
      return MergeCommandResult.success(getTargetRevision().name(), revCommit.name(), extractRevisionFromRevCommit(revCommit));
    } else {
      return analyseFailure(result);
    }
  }
}
