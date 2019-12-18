package sonia.scm.repository.spi;

import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.revwalk.RevCommit;
import sonia.scm.ContextEntry;
import sonia.scm.repository.InternalRepositoryException;

import java.util.Optional;

public class GitMerger {

  static String evaluateRevisionFromMergeCommit(Optional<RevCommit> revCommit) {
    if (revCommit.isPresent()) {
      return revCommit.get().toString().split(" ")[1];
    }
    throw new InternalRepositoryException(ContextEntry.ContextBuilder.entity(GitMerger.class, "merge commit failed"), "could not create commit on merge");
  }
}
