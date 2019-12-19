package sonia.scm.repository.spi;

import org.eclipse.jgit.revwalk.RevCommit;

import java.util.Optional;

public class GitRevisionExtractor {

  static String extractRevisionFromRevCommit(RevCommit revCommit) {
    return revCommit.toString().split(" ")[1];
  }
}
