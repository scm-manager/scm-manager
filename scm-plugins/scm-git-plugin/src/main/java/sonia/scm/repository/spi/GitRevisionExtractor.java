package sonia.scm.repository.spi;

import org.eclipse.jgit.revwalk.RevCommit;

import java.util.Optional;

public class GitRevisionExtractor {

  static String extractRevisionFromRevCommit(Optional<RevCommit> revCommit) {
    if (revCommit.isPresent()) {
      return revCommit.get().toString().split(" ")[1];
    }
    return "";
  }
}
